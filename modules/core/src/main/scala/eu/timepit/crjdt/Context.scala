package eu.timepit.crjdt

import cats.syntax.order._
import eu.timepit.crjdt.Context.Ptr.{HeadP, IdP, TailP}
import eu.timepit.crjdt.Context.{ListCtx, MapCtx, Ptr, RegCtx}
import eu.timepit.crjdt.Key.{HeadK, IdK}
import eu.timepit.crjdt.Operation.Mutation
import eu.timepit.crjdt.Operation.Mutation.{AssignM, DeleteM, InsertM}
import eu.timepit.crjdt.Tag.{ListT, MapT, RegT}
import eu.timepit.crjdt.Val.{EmptyList, EmptyMap}

sealed trait Context extends Product with Serializable {
  def next(cur: Cursor): Cursor =
    cur match {
      case Cursor(Vector(), k) =>
        val k1Ptr = getNextPtr(Ptr.fromKey(k))
        k1Ptr match {
          case TailP => cur
          case _ =>
            val cur1 = Cursor.withFinalKey(k1Ptr.toKey)
            // NEXT2
            if (getPres(k).nonEmpty) cur1
            // NEXT3
            else next(cur1)
        }

      // NEXT4
      case Cursor(k1 +: _, _) =>
        findChild(k1).fold(cur) { child =>
          val cur1 = child.next(cur.dropFirst)
          cur.copy(finalKey = cur1.finalKey)
        }
    }

  def applyOp(op: Operation): Context =
    op.cur match {
      case Cursor(Vector(), k) =>
        op.mut match {
          // EMPTY-MAP, EMPTY-LIST
          case mut @ AssignM(EmptyMap | EmptyList) =>
            val tag = if (mut.value == EmptyMap) MapT(k) else ListT(k)
            val (ctx1, _) = clearElem(op.deps, k)
            val ctx2 = ctx1.addId(tag, op.id, op.mut)
            val child = ctx2.getChild(tag)
            ctx2.addCtx(tag, child)

          // ASSIGN
          case AssignM(value) =>
            val tag = RegT(k)
            val (ctx1, _) = clear(op.deps, tag)
            val ctx2 = ctx1.addId(tag, op.id, op.mut)
            val child = ctx2.getChild(tag)
            ctx2.addCtx(tag, child.addRegValue(op.id, value))

          case InsertM(value) =>
            val prevPtr = Ptr.fromKey(k)
            val nextPtr = getNextPtr(prevPtr)
            nextPtr match {
              // INSERT2
              case IdP(nextId) if op.id < nextId =>
                applyOp(op.copy(cur = Cursor.withFinalKey(IdK(nextId))))

              // INSERT1
              case _ =>
                val idPtr = IdP(op.id)
                val ctx1 = applyOp(
                  op.copy(cur = Cursor.withFinalKey(IdK(op.id)),
                          mut = AssignM(value)))
                ctx1.setNextPtr(prevPtr, idPtr).setNextPtr(idPtr, nextPtr)
            }

          // DELETE
          case DeleteM =>
            val (ctx1, _) = clearElem(op.deps, k)
            ctx1
        }

      // DESCEND
      case Cursor(k1 +: _, _) =>
        val child0 = getChild(k1)
        val child1 = child0.applyOp(op.copy(cur = op.cur.dropFirst))
        val ctx1 = addId(k1, op.id, op.mut)
        ctx1.addCtx(k1, child1)
    }

  def addCtx(tag: Tag, ctx: Context): Context =
    this match {
      case m: MapCtx => m.copy(entries = m.entries.updated(tag, ctx))
      case l: ListCtx => l.copy(entries = l.entries.updated(tag, ctx))
      case _: RegCtx => this
    }

  def addRegValue(id: Id, value: Val): Context =
    this match {
      case r: RegCtx => r.copy(values = r.values.updated(id, value))
      case _ => this
    }

  def getRegValues: RegValues =
    this match {
      case r: RegCtx => r.values
      case _ => Map.empty
    }

  def addId(tag: Tag, id: Id, mut: Mutation): Context =
    mut match {
      // ADD-ID2
      case DeleteM => this
      // ADD-ID1
      case _ => setPres(tag.key, getPres(tag.key) + id)
    }

  def clearElem(deps: Set[Id], key: Key): (Context, Set[Id]) = {
    val (ctx1, pres1) = clearAny(deps, key)
    val pres2 = ctx1.getPres(key)
    val pres3 = (pres1 ++ pres2) -- deps
    (ctx1.setPres(key, pres3), pres3)
  }

  // CLEAR-ANY
  def clearAny(deps: Set[Id], key: Key): (Context, Set[Id]) = {
    val ctx0 = this
    val (ctx1, pres1) = ctx0.clear(deps, MapT(key))
    val (ctx2, pres2) = ctx1.clear(deps, ListT(key))
    val (ctx3, pres3) = ctx2.clear(deps, RegT(key))
    (ctx3, pres1 ++ pres2 ++ pres3)
  }

  def clear(deps: Set[Id], tag: Tag): (Context, Set[Id]) =
    findChild(tag) match {
      // CLEAR-NONE
      case None => (this, Set.empty)
      case Some(child) =>
        tag match {
          // CLEAR-REG
          case tag: RegT =>
            val concurrent = child.getRegValues.filterKeys(id => !deps(id))
            (addCtx(tag, RegCtx(concurrent)), concurrent.keySet)

          // CLEAR-MAP1
          case tag: MapT =>
            val (cleared, pres) = child.clearMap(deps, Set.empty)
            (addCtx(tag, cleared), pres)

          // CLEAR-LIST1
          case _: ListT =>
            (this, Set.empty) // TODO
        }
    }

  // CLEAR-MAP2, CLEAR-MAP3
  def clearMap(deps: Set[Id], done: Set[Key]): (Context, Set[Id]) = {
    val keys = keySet.map(_.key)
    (keys -- done).headOption.fold((this, Set.empty[Id])) { k =>
      val (ctx1, pres1) = clearElem(deps, k)
      val (ctx2, pres2) = ctx1.clearMap(deps, done + k)
      (ctx2, pres1 ++ pres2)
    }
  }

  def getChild(tag: Tag): Context =
    findChild(tag).getOrElse {
      tag match {
        // CHILD-MAP
        case _: MapT => Context.emptyMap
        // CHILD-LIST
        case _: ListT => Context.emptyList
        // CHILD-REG
        case _: RegT => Context.emptyReg
      }
    }

  // CHILD-GET
  def findChild(tag: Tag): Option[Context] =
    this match {
      case m: MapCtx => m.entries.get(tag)
      case l: ListCtx => l.entries.get(tag)
      case _: RegCtx => None
    }

  // PRESENCE1, PRESENCE2
  def getPres(key: Key): Set[Id] =
    this match {
      case m: MapCtx => m.presSets.getOrElse(key, Set.empty)
      case l: ListCtx => l.presSets.getOrElse(key, Set.empty)
      case _: RegCtx => Set.empty
    }

  def setPres(key: Key, pres: Set[Id]): Context =
    this match {
      case m: MapCtx => m.copy(presSets = m.presSets.updated(key, pres))
      case l: ListCtx => l.copy(presSets = l.presSets.updated(key, pres))
      case _: RegCtx => this
    }

  def keySet: Set[Tag] =
    this match {
      case m: MapCtx => m.entries.keySet
      case _ => Set.empty
    }

  def getNextPtr(ptr: Ptr): Ptr =
    this match {
      case l: ListCtx => l.order.getOrElse(ptr, TailP)
      case _ => TailP
    }

  def setNextPtr(src: Ptr, dst: Ptr): Context =
    this match {
      case l: ListCtx => l.copy(order = l.order.updated(src, dst))
      case _ => this
    }
}

object Context {
  final case class MapCtx(entries: Map[Tag, Context],
                          presSets: Map[Key, Set[Id]])
      extends Context

  final case class ListCtx(entries: Map[Tag, Context],
                           presSets: Map[Key, Set[Id]],
                           order: Map[Ptr, Ptr])
      extends Context

  final case class RegCtx(values: RegValues) extends Context

  ///

  sealed trait Ptr extends Product with Serializable {
    def toKey: Key =
      this match {
        case IdP(id) => IdK(id)
        case HeadP => HeadK
        case _ => HeadK
      }
  }

  object Ptr {
    final case class IdP(id: Id) extends Ptr
    case object HeadP extends Ptr
    case object TailP extends Ptr

    def fromKey(key: Key): Ptr =
      key match {
        case HeadK => HeadP
        case IdK(id) => IdP(id)
        case _ => TailP
      }
  }

  ///

  def emptyMap: Context =
    MapCtx(Map.empty, Map.empty)

  def emptyList: Context =
    ListCtx(Map.empty, Map.empty, Map(HeadP -> TailP))

  def emptyReg: Context =
    RegCtx(Map.empty)
}
