package eu.timepit.crjdt

import eu.timepit.crjdt.Context.{ListCtx, MapCtx, RegCtx}
import eu.timepit.crjdt.Operation.Mutation
import eu.timepit.crjdt.Operation.Mutation.{AssignM, DeleteM, InsertM}
import eu.timepit.crjdt.Tag.{ListT, MapT, RegT}
import eu.timepit.crjdt.Val.{EmptyList, EmptyMap}

sealed trait Context extends Product with Serializable {
  def applyOp(op: Operation): Context =
    op.cur match {
      case Cursor(Vector(), k) =>
        op.mut match {
          // EMPTY-MAP, EMPTY-LIST
          case mut @ AssignM(EmptyMap | EmptyList) =>
            val ctx1 = this // TODO: clearElem
            val tag = if (mut.v == EmptyMap) MapT(k) else ListT(k)
            val ctx2 = ctx1.addId(tag, op.id, op.mut)
            val child = ctx2.getChild(tag)
            ctx2.addCtx(tag, child)

          // ASSIGN
          case AssignM(value) =>
            val ctx1 = this // TODO: clear
            val tag = RegT(k)
            val ctx2 = ctx1.addId(tag, op.id, op.mut)
            val child = ctx2.getChild(tag)
            ctx2.addCtx(tag, child.addValue(op.id, value))

          // INSERT1, INSERT2
          case InsertM(v) => ???

          // DELETE
          case DeleteM => ???
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
      case l: ListCtx => ???
      case r: RegCtx => r
    }

  def addValue(id: Id, value: Val): Context =
    this match {
      case r: RegCtx => r.copy(values = r.values.updated(id, value))
      case _ => this
    }

  def addId(tag: Tag, id: Id, mut: Mutation): Context =
    mut match {
      // ADD-ID2
      case DeleteM => this
      // ADD-ID1
      case _ => setPres(tag.key, getPres(tag.key) + id)
    }

  def getChild(tag: Tag): Context =
    findChild(tag).getOrElse {
      tag match {
        // CHILD-MAP
        case _: MapT => MapCtx(Map.empty, Map.empty)
        // CHILD-LIST
        case _: ListT => ???
        // CHILD-REG
        case _: RegT => RegCtx(Map.empty)
      }
    }

  // CHILD-GET
  def findChild(tag: Tag): Option[Context] =
    this match {
      case m: MapCtx => m.entries.get(tag)
      case l: ListCtx => ???
      case _: RegCtx => None
    }

  // PRESENCE1, PRESENCE2
  def getPres(key: Key): Set[Id] =
    this match {
      case m: MapCtx => m.presSets.getOrElse(key, Set.empty)
      case l: ListCtx => ???
      case _: RegCtx => Set.empty
    }

  def setPres(key: Key, pres: Set[Id]): Context =
    this match {
      case m: MapCtx => m.copy(presSets = m.presSets.updated(key, pres))
      case l: ListCtx => ???
      case _: RegCtx => this
    }
}

object Context {
  final case class MapCtx(entries: Map[Tag, Context],
                          presSets: Map[Key, Set[Id]])
      extends Context

  final case class ListCtx() extends Context

  final case class RegCtx(values: Map[Id, Val]) extends Context

  ///

  def empty: Context =
    MapCtx(Map.empty, Map.empty)
}
