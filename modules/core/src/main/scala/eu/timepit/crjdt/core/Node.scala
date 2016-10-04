package eu.timepit.crjdt.core

import cats.instances.set._
import cats.syntax.order._
import eu.timepit.crjdt.core.Key.{IdK, StrK}
import eu.timepit.crjdt.core.ListRef.{HeadR, IdR, TailR}
import eu.timepit.crjdt.core.Mutation.{AssignM, DeleteM, InsertM}
import eu.timepit.crjdt.core.Node.{ListNode, MapNode, RegNode}
import eu.timepit.crjdt.core.TypeTag.{ListT, MapT, RegT}
import eu.timepit.crjdt.core.Val.EmptyMap
import eu.timepit.crjdt.core.util.removeOrUpdate

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
          case Some(map: MapNode) =>
            map.presSets.collect { case (StrK(key), v) if v.nonEmpty => key }.toSet
          case _ => Set.empty
        }

      // KEYS3
      case Cursor.Branch(k1, cur1) =>
        findChild(k1) match {
          case Some(child) => child.keys(cur1)
          case None => Set.empty
        }
    }

  @tailrec
  final def values(cur: Cursor): List[LeafVal] =
    cur.view match {
      // VAL2
      case Cursor.Leaf(k) =>
        findChild(RegT(k)) match {
          case Some(reg: RegNode) => reg.values.values.toList
          case _ => List.empty
        }

      // VAL3
      case Cursor.Branch(k1, cur1) =>
        findChild(k1) match {
          case Some(child) => child.values(cur1)
          case None => List.empty
        }
    }

  final def applyOp(op: Operation): Node =
    op.cur.view match {
      case Cursor.Leaf(k) =>
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

          case InsertM(value) =>
            val prevRef = ListRef.fromKey(k)
            val nextRef = getNextRef(prevRef)
            nextRef match {
              // INSERT2
              case IdR(nextId) if op.id < nextId =>
                applyOp(op.copy(cur = Cursor.withFinalKey(IdK(nextId))))

              // INSERT1
              case _ =>
                val idRef = IdR(op.id)
                val ctx1 = applyOp(
                  op.copy(cur = Cursor.withFinalKey(IdK(op.id)),
                          mut = AssignM(value)))
                ctx1.setNextRef(prevRef, idRef).setNextRef(idRef, nextRef)
            }

          // DELETE
          case DeleteM =>
            val (ctx1, _) = clearElem(op.deps, k)
            ctx1
        }

      // DESCEND
      case Cursor.Branch(k1, cur1) =>
        val child0 = getChild(k1)
        val child1 = child0.applyOp(op.copy(cur = cur1))
        val ctx1 = addId(k1, op.id, op.mut)
        ctx1.addNode(k1, child1)
    }

  final def addNode(tag: TypeTag, node: Node): Node =
    this match {
      case m: MapNode => m.copy(entries = m.entries.updated(tag, node))
      case l: ListNode => l.copy(entries = l.entries.updated(tag, node))
      case _: RegNode => this
    }

  final def addRegValue(id: Id, value: LeafVal): Node =
    this match {
      case r: RegNode => r.copy(values = r.values.updated(id, value))
      case _ => this
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

  final def clear(deps: Set[Id], tag: TypeTag): (Node, Set[Id]) =
    findChild(tag) match {
      // CLEAR-NONE
      case None => (this, Set.empty)

      // CLEAR-REG
      case Some(child: RegNode) =>
        val concurrent = child.values.filterKeys(id => !deps(id))
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
      case m: MapNode => m.entries.get(tag)
      case l: ListNode => l.entries.get(tag)
      case _: RegNode => None
    }

  // PRESENCE1, PRESENCE2
  final def getPres(key: Key): Set[Id] =
    this match {
      case m: MapNode => m.presSets.getOrElse(key, Set.empty)
      case l: ListNode => l.presSets.getOrElse(key, Set.empty)
      case _: RegNode => Set.empty
    }

  final def setPres(key: Key, pres: Set[Id]): Node =
    this match {
      case m: MapNode =>
        m.copy(presSets = removeOrUpdate(m.presSets, key, pres))
      case l: ListNode =>
        l.copy(presSets = removeOrUpdate(l.presSets, key, pres))
      case _: RegNode =>
        this
    }

  final def keySet: Set[TypeTag] =
    this match {
      case m: MapNode => m.entries.keySet
      case _ => Set.empty
    }

  final def getNextRef(ref: ListRef): ListRef =
    this match {
      case l: ListNode => l.order.getOrElse(ref, TailR)
      case _ => TailR
    }

  final def setNextRef(src: ListRef, dst: ListRef): Node =
    this match {
      case l: ListNode => l.copy(order = l.order.updated(src, dst))
      case _ => this
    }
}

object Node {
  final case class MapNode(entries: Map[TypeTag, Node],
                           presSets: Map[Key, Set[Id]])
      extends Node

  final case class ListNode(entries: Map[TypeTag, Node],
                            presSets: Map[Key, Set[Id]],
                            order: Map[ListRef, ListRef])
      extends Node

  final case class RegNode(values: RegValues) extends Node

  ///

  final def emptyMap: Node =
    MapNode(entries = Map.empty, presSets = Map.empty)

  final def emptyList: Node =
    ListNode(entries = Map.empty,
             presSets = Map.empty,
             order = Map(HeadR -> TailR))

  final def emptyReg: Node =
    RegNode(values = Map.empty)
}
