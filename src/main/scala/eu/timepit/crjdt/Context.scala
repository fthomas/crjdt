package eu.timepit.crjdt

import eu.timepit.crjdt.Context._
import eu.timepit.crjdt.Operation.Mutation
import eu.timepit.crjdt.Operation.Mutation.{AssignM, DeleteM, InsertM}
import eu.timepit.crjdt.Tag.{ListT, MapT, RegT}
import eu.timepit.crjdt.Val.{EmptyList, EmptyMap}

sealed trait Context extends Product with Serializable {
  def key: Tag

  // PRESENCE1, PRESENCE2
  def pres: Set[Id]

  def applyOp(op: Operation): Context =
    op.cur match {
      case Cursor(Vector(), k) =>
        op.mut match {
          // EMPTY-MAP
          case AssignM(EmptyMap) => ???

          // EMPTY-LIST
          case AssignM(EmptyList) => ???

          // ASSIGN
          case AssignM(v) => ???

          // INSERT1, INSERT2
          case InsertM(v) => ???

          // DELETE
          case DeleteM => ???
        }

      // DESCEND
      case Cursor(k +: _, _) =>
        val child = child2(k)
        val cur2 = op.cur.dropFirst
        val op2 = op.copy(cur = cur2)
        val childp = child.applyOp(op2)
        val ctxp = addId(k, op.id, op.mut)
        val assoc = AssocCtx(k, childp, Set.empty)
        // TODO: ctxp [assoc], i.e. add assoc to ctxp
        ???
    }

  def addAssoc(assoc: AssocCtx): Context =
    this match {
      case MapCtx(k, p, c) => ???
      case ListCtx(_, _, _) => ???
      case _ => ???
    }

  def addId(key: Tag, id: Id, mut: Mutation): Context =
    mut match {
      // ADD-ID2
      case DeleteM => this

      // ADD-ID1
      case _ => if (key == this.key) withPres(pres + id) else this
    }

  def getChild(key: Tag): Option[Context] =
    this match {
      case AssocCtx(k, ctx, _) if k == key => Some(ctx)
      case MapCtx(_, _, children) => children.get(key)
      case ListCtx(_, _, children) => ??? // children
      // in lists we have to perform a linear search for key
      // since the cursor just contains the Id of the element
      // (or HeadK)
      case RegCtx(k, _, _) => None
    }

  def child2(key: Tag): Context =
    getChild(key) match {
      case Some(x) => x
      case None =>
        key match {
          // CHILD-MAP
          case k @ MapT(_) => MapCtx(k, Set.empty, Map.empty)
          // CHILD-LIST
          case k @ ListT(_) => ListCtx(k, Set.empty, CtxList.empty)
          // CHILD-REG
          case k @ RegT(_) => RegCtx(k, Set.empty, Map.empty)
        }
    }

  def withPres(pres: Set[Id]): Context =
    this match {
      case ctx: MapCtx => ctx.copy(pres = pres)
      case ctx: ListCtx => ctx.copy(pres = pres)
      case ctx: RegCtx => ctx.copy(pres = pres)
      case ctx: AssocCtx => ctx.copy(pres = pres)
    }
}

object Context {
  // ctx must not be AssocCtx
  final case class AssocCtx(key: Tag, ctx: Context, pres: Set[Id])
      extends Context

  final case class MapCtx(key: MapT,
                          pres: Set[Id],
                          children: Map[Tag, AssocCtx])
      extends Context

  final case class ListCtx(key: ListT, pres: Set[Id], children: CtxList)
      extends Context

  final case class RegCtx(key: RegT, pres: Set[Id], values: RegValues)
      extends Context

  ///

  trait CtxList extends Product with Serializable

  object CtxList {
    final case class Head(next: CtxList) extends CtxList
    final case class Node(ctx: AssocCtx, next: CtxList) extends CtxList
    case object Tail extends CtxList

    def empty: CtxList =
      Head(Tail)
  }

  ///

  def emptyDoc: Context =
    MapCtx(MapT(Key.DocK), Set.empty, Map.empty)
}
