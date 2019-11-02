package eu.timepit.crjdt.core

import cats.instances.set._
import cats.syntax.order._
import eu.timepit.crjdt.core.Key.{IdK, StrK}
import eu.timepit.crjdt.core.ListRef.{HeadR, IdR, TailR}
import eu.timepit.crjdt.core.Mutation.{AssignM, DeleteM, InsertM, MoveVerticalM}
import eu.timepit.crjdt.core.Node.{ListNode, MapNode, RegNode}
import eu.timepit.crjdt.core.TypeTag.{ListT, MapT, RegT}
import eu.timepit.crjdt.core.Val.EmptyMap
import eu.timepit.crjdt.core.util.removeOrUpdate

import scala.PartialFunction._
import scala.annotation.tailrec

sealed trait Node extends Product with Serializable {
  final def next(cur: Cursor): Cursor =
    cur.view match {
      case Cursor.Leaf(k) =>
        val k1Ref = getNextRef(ListRef.fromKey(k))
        k1Ref match {
          case TailR => cur
          case keyRef: KeyRef =>
            val k1 = keyRef.toKey
            val cur1 = Cursor.withFinalKey(k1)
            // NEXT2
            if (getPres(k1).nonEmpty) cur1
            // NEXT3
            else next(cur1)
        }

      // NEXT4
      case Cursor.Branch(k1, cur1) =>
        findChild(k1).fold(cur) { child =>
          val cur2 = child.next(cur1)
          cur.copy(finalKey = cur2.finalKey)
        }
    }

  @tailrec
  final def keys(cur: Cursor): Set[String] =
    cur.view match {
      // KEYS2
      case Cursor.Leaf(k) =>
        findChild(MapT(k)) match {
          case Some(map: MapNode) => map.keys
          case _                  => Set.empty
        }

      // KEYS3
      case Cursor.Branch(k1, cur1) =>
        findChild(k1) match {
          case Some(child) => child.keys(cur1)
          case None        => Set.empty
        }
    }

  @tailrec
  final def values(cur: Cursor): List[LeafVal] =
    cur.view match {
      // VAL2
      case Cursor.Leaf(k) =>
        findChild(RegT(k)) match {
          case Some(reg: RegNode) => reg.values
          case _                  => List.empty
        }

      // VAL3
      case Cursor.Branch(k1, cur1) =>
        findChild(k1) match {
          case Some(child) => child.values(cur1)
          case None        => List.empty
        }
    }

  /** If this node is a list: Save the order of item in the list.
    * Don't overwrite if it already exists. Why? Assumed it would be overwritten
    * and user1, user2 and user3 do an op concurrently. When user2's op arrives
    * at user1, the order is reset and both ops are redone. Each time an op is
    * applied, the order is saved. Therefore when the user2's op is applied, the
    * order is saved. That's the order after user1's op was applied. When now
    * user3's op arrives and the order is reset, the order we reset to should be
    * the order before all three ops were applied. But it's not, since user2's op
    * has overwritten the order. Therefore don't overwrite. */
  def saveOrder(op: Operation): Node =
    this match {
      case ln: ListNode if (ln.orderArchive get op.id.c).isEmpty =>
        ln.copy(orderArchive = ln.orderArchive + (op.id.c -> ln.order))
      case _ => this
    }

  final def applyOp(op: Operation, replica: Replica): Node =
    op.cur.view match {
      case Cursor.Leaf(_) =>
        this match {
          // If this node is a list: To merge concurrent vertical move ops,
          // we may have to redo concurrent ops.
          case ln: ListNode =>
            // Returns ops which are processed, are as new or
            // newer, changed the linked list and have the same parent as
            // the current op.
            def concurrentOpsSince(count: BigInt): Vector[Operation] = {
              val allOps = replica.generatedOps ++ replica.receivedOps

              for (o <- allOps
                   if (replica.processedOps.contains(o.id) && cond(o.mut) {
                     case InsertM(_) | DeleteM | MoveVerticalM(_, _) => true
                   } || o.id == op.id) && o.id.c >= count && this
                     .findChild(RegT(o.cur.finalKey))
                     .isDefined)
                yield o
            }

            val concurrentOps = concurrentOpsSince(op.id.c)

            // If there are concurrent or newer local ops other than the
            //  incoming op: Restore the old order, then redo these ops.
            //  However, it is only necessary if a MoveVertical op is among them.
            //  Therefore we do it only then.
            if (concurrentOps.length > 1 && concurrentOps.count(
                  _.mut.isInstanceOf[MoveVerticalM]
                ) >= 1) {
              //  Before applying an operation we save the order in orderArchive.
              // It is a Map whose key is the lamport timestamp counter value.
              // To improve performance and save disk space, we don't save the
              // order before assign operations, since they don't change the order.
              // Now there might be this situation: Alice did an assign and then a
              // move op, while Bob did a move op. Now Bobs op comes in and
              // Alice resets her order to the order with counter value like the
              // incoming op. However, locally exists no such saved order, since
              // she has done an assign op at that count. Therefore she resets
              // to the next higher saved order.
              // This fix is implemented by getting all orders whose counter is
              // greater equals than the counter of the incoming op and then
              // choosing the earliest order of those:
              val newerOrders = ln.orderArchive.filterKeys(_ >= op.id.c)

              // restore the order
              val ctx1 =
                if (newerOrders.nonEmpty) ln.copy(order = newerOrders.minBy {
                  case (c, _) => c
                }._2)
                else this

              // redo newer ops in a specific order
              ctx1.applyMany(concurrentOps.sortWith(_.id < _.id), replica)
            } else {
              // The op was done without me doing an op concurrently, so there is
              // no need to restore anything. Just apply the op.
              applyAtLeaf(op, replica)
            }
          case _ =>
            applyAtLeaf(op, replica)
        }

      // DESCEND
      case Cursor.Branch(k1, cur1) =>
        val child0 = getChild(k1)
        val child1 = child0.applyOp(op.copy(cur = cur1), replica) // update that child
        // The DESCEND rule also invokes ADD-ID1,2 at each tree
        // node along the path described by the cursor, adding the
        // operation ID to the presence set pres(k) to indicate that the
        // subtree includes a mutation made by this operation.
        val ctx1 = addId(k1, op.id, op.mut)
        ctx1.addNode(k1, child1)
    }

  def applyAtLeaf(op: Operation, replica: Replica): Node = {
    val k = op.cur.finalKey
    op.mut match {
      // EMPTY-MAP, EMPTY-LIST
      case AssignM(value: BranchVal) =>
        val tag = if (value == EmptyMap) MapT(k) else ListT(k)
        val (ctx1, _) = clearElem(op.deps, k)
        val ctx2 = ctx1.addId(tag, op.id, op.mut)
        val child = ctx2.getChild(tag)
        ctx2.addNode(tag, child)

      // ASSIGN
      case AssignM(value: LeafVal) =>
        val tag = RegT(k)
        val (ctx1, _) = clear(op.deps, tag)
        val ctx2 = ctx1.addId(tag, op.id, op.mut)
        val child = ctx2.getChild(tag)
        ctx2.addNode(tag, child.addRegValue(op.id, value))

      // DELETE
      case DeleteM =>
        val ctx1 = saveOrder(op)
        val (ctx2, _) = ctx1.clearElem(op.deps, k)
        ctx2

      // INSERT
      case InsertM(value) =>
        val prevRef = ListRef.fromKey(k)
        val nextRef = getNextRef(prevRef)
        nextRef match {
          // INSERT2
          // INSERT 2 handles the case of multiple replicas concurrently
          // inserting list elements at the same position, and uses the
          // ordering relation < on Lamport timestamps to consistently
          // determine the insertion point.
          case IdR(nextId) if op.id < nextId =>
            // Normally, the nextId is lower, since it was already inserted and
            // newer ops have a higher id. NextId may be only higher, if two
            // insert operations are concurrent the one the other one - with the
            // higher id - was already inserted. When that's the case, insert
            // the new op after the concurrent one with higher id. This way, when
            // inserted at the same place, the op whose user id is higher,
            // comes always fist.
            applyAtLeaf(
              op.copy(cur = Cursor.withFinalKey(IdK(nextId))),
              replica
            )

          // INSERT1
          // INSERT1 performs the insertion by manipulating the linked
          // list structure.
          case _ =>
            val idRef = IdR(op.id)
            // the ID of the inserted node will be the ID of the operation
            val ctx1 = applyAtLeaf(
              op.copy(
                cur = Cursor.withFinalKey(IdK(op.id)),
                mut = AssignM(value)
              ),
              replica
            )
            val ctx2 = ctx1.saveOrder(op)
            ctx2.setNextRef(prevRef, idRef).setNextRef(idRef, nextRef)
        }

      // MOVE-VERTICAL
      case MoveVerticalM(targetCursor, aboveBelow) =>
        val movedNodeRef = ListRef.fromKey(op.cur.finalKey)
        val nodeAfterMovedNodeRef = getNextRef(movedNodeRef)

        val targetNodeRef = ListRef.fromKey(targetCursor.finalKey)
        val nodeAfterTargetNodeRef = getNextRef(targetNodeRef)

        if (// the order is already as wished
            aboveBelow == Before && nodeAfterMovedNodeRef == targetNodeRef ||
            aboveBelow == After && nodeAfterTargetNodeRef == movedNodeRef ||
            movedNodeRef == targetNodeRef ||
            // the target has not the same parent
            this.findChild(RegT(targetCursor.finalKey)).isEmpty) {
          this
        } else {
          val ctx0 = saveOrder(op)

          // Fix the whole where we removed the moved node:
          // Find the node which points to the moved node and set its
          // next pointer to the node the moved node points to.
          val ctx1 =
            ctx0.setNextRef(getPreviousRef(movedNodeRef), nodeAfterMovedNodeRef)

          // Insert the moved node somewhere else by adjusting the pointers.
          aboveBelow match {
            case Before =>
              ctx1
                .setNextRef(getPreviousRef(targetNodeRef), movedNodeRef)
                .setNextRef(movedNodeRef, targetNodeRef)
            case After =>
              ctx1
                .setNextRef(targetNodeRef, movedNodeRef)
                .setNextRef(movedNodeRef, nodeAfterTargetNodeRef)
          }
        }
    }
  }

  def applyMany(operations: Vector[Operation], replica: Replica): Node =
    operations match {
      case o +: ops =>
        val ctx = applyAtLeaf(o, replica)
        ctx.applyMany(ops, replica)
      case _ => this
    }

  /** Append a @param node (which is an ID, value pair) to the child specified
    * by the @param tag (which has a key inside). */
  final def addNode(tag: TypeTag, node: Node): Node =
    this match {
      case n: BranchNode => n.withChildren(n.children.updated(tag, node))
      case _             => this
    }

  final def addRegValue(id: Id, value: LeafVal): Node =
    this match {
      case r: RegNode => r.copy(regValues = r.regValues.updated(id, value))
      case _          => this
    }

  final def addId(tag: TypeTag, id: Id, mut: Mutation): Node =
    mut match {
      // ADD-ID2
      case DeleteM => this
      // ADD-ID1
      case _ => setPres(tag.key, getPres(tag.key) + id)
    }

  final def clearElem(deps: Set[Id], key: Key): (Node, Set[Id]) = {
    val (ctx1, pres1) = clearAny(deps, key)
    val pres2 = ctx1.getPres(key)
    val pres3 = (pres1 ++ pres2) -- deps
    (ctx1.setPres(key, pres3), pres3)
  }

  // CLEAR-ANY
  final def clearAny(deps: Set[Id], key: Key): (Node, Set[Id]) = {
    val ctx0 = this
    val (ctx1, pres1) = ctx0.clear(deps, MapT(key))
    val (ctx2, pres2) = ctx1.clear(deps, ListT(key))
    val (ctx3, pres3) = ctx2.clear(deps, RegT(key))
    (ctx3, pres1 ++ pres2 ++ pres3)
  }

  /** Clears the prior value at the cursor. */
  final def clear(deps: Set[Id], tag: TypeTag): (Node, Set[Id]) =
    findChild(tag) match {
      // CLEAR-NONE
      case None => (this, Set.empty)

      // CLEAR-REG
      case Some(child: RegNode) =>
        val concurrent = child.regValues.filterKeys(id => !deps(id))
        (addNode(tag, RegNode(concurrent)), concurrent.keySet)

      // CLEAR-MAP1
      case Some(child: MapNode) =>
        val (cleared, pres) = child.clearMap(deps, Set.empty)
        (addNode(tag, cleared), pres)

      // CLEAR-LIST1
      case Some(child: ListNode) =>
        val (cleared, pres) = child.clearList(deps, HeadR)
        (addNode(tag, cleared), pres)
    }

  // CLEAR-MAP2, CLEAR-MAP3
  final def clearMap(deps: Set[Id], done: Set[Key]): (Node, Set[Id]) = {
    val keys = keySet.map(_.key)
    (keys -- done).headOption match {
      case None => (this, Set.empty)
      case Some(key) =>
        val (ctx1, pres1) = clearElem(deps, key)
        val (ctx2, pres2) = ctx1.clearMap(deps, done + key)
        (ctx2, pres1 ++ pres2)
    }
  }

  final def clearList(deps: Set[Id], ref: ListRef): (Node, Set[Id]) =
    ref match {
      // CLEAR-LIST3
      case TailR => (this, Set.empty)
      // CLEAR-LIST2
      case keyRef: KeyRef =>
        val nextRef = getNextRef(ref)
        val (ctx1, pres1) = clearElem(deps, keyRef.toKey)
        val (ctx2, pres2) = ctx1.clearList(deps, nextRef)
        (ctx2, pres1 ++ pres2)
    }

  final def getChild(tag: TypeTag): Node =
    findChild(tag).getOrElse {
      tag match {
        // CHILD-MAP
        case _: MapT => Node.emptyMap
        // CHILD-LIST
        case _: ListT => Node.emptyList
        // CHILD-REG
        case _: RegT => Node.emptyReg
      }
    }

  // CHILD-GET
  final def findChild(tag: TypeTag): Option[Node] =
    this match {
      case n: BranchNode => n.children.get(tag)
      case _             => None
    }

  // PRESENCE1, PRESENCE2
  final def getPres(key: Key): Set[Id] =
    this match {
      case n: BranchNode => n.presSets.getOrElse(key, Set.empty)
      case _             => Set.empty
    }

  final def setPres(key: Key, pres: Set[Id]): Node =
    this match {
      case n: BranchNode =>
        n.withPresSets(removeOrUpdate(n.presSets, key, pres))
      case _ =>
        this
    }

  final def keySet: Set[TypeTag] =
    this match {
      case m: MapNode => m.children.keySet
      case _          => Set.empty
    }

  final def getNextRef(ref: ListRef): ListRef =
    this match {
      case l: ListNode => l.order.getOrElse(ref, TailR)
      case _           => TailR
    }

  final def getPreviousRef(ref: ListRef): ListRef =
    this match {
      case l: ListNode => l.order.map(_.swap).getOrElse(ref, HeadR)
      case _           => HeadR
    }

  final def setNextRef(src: ListRef, dst: ListRef): Node =
    this match {
      case l: ListNode => l.copy(order = l.order.updated(src, dst))
      case _           => this
    }
}

sealed trait BranchNode extends Node {
  def children: Map[TypeTag, Node]

  def presSets: Map[Key, Set[Id]]

  def withChildren(children: Map[TypeTag, Node]): BranchNode

  def withPresSets(presSets: Map[Key, Set[Id]]): BranchNode
}

object Node {
  final case class MapNode(
      children: Map[TypeTag, Node],
      presSets: Map[Key, Set[Id]]
  ) extends BranchNode {
    override def withChildren(children: Map[TypeTag, Node]): MapNode =
      copy(children = children)

    override def withPresSets(presSets: Map[Key, Set[Id]]): MapNode =
      copy(presSets = presSets)

    def keys: Set[String] =
      presSets.collect { case (StrK(key), pres) if pres.nonEmpty => key }.toSet
  }

  final case class ListNode(
      children: Map[TypeTag, Node],
      presSets: Map[Key, Set[Id]],
      order: Map[ListRef, ListRef],
      orderArchive: Map[BigInt, Map[ListRef, ListRef]]
  ) extends BranchNode {
    override def withChildren(children: Map[TypeTag, Node]): ListNode =
      copy(children = children)

    override def withPresSets(presSets: Map[Key, Set[Id]]): ListNode =
      copy(presSets = presSets)

    /** The tests cannot converge, since the orderArchive of two replicas is
      * always different. Therefore don't compare the orderArchive. */
    override def equals(that: scala.Any): Boolean =
      that match {
        case ln: ListNode => {
          children == ln.children && presSets == ln.presSets && order == ln.order
        }
        case _ => super.equals(that)
      }
  }

  final case class RegNode(regValues: Map[Id, LeafVal]) extends Node {
    def values: List[LeafVal] =
      regValues.values.toList
  }

  final def emptyMap: Node =
    MapNode(children = Map.empty, presSets = Map.empty)

  final def emptyList: Node =
    ListNode(
      children = Map.empty,
      presSets = Map.empty,
      order = Map(HeadR -> TailR),
      orderArchive = Map()
    )

  final def emptyReg: Node =
    RegNode(regValues = Map.empty)
}
