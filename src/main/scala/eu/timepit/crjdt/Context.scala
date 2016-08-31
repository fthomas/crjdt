package eu.timepit.crjdt

import eu.timepit.crjdt.Context.{ListCtx, MapCtx, RegCtx}
import eu.timepit.crjdt.Operation.Mutation
import eu.timepit.crjdt.Operation.Mutation.DeleteM
import eu.timepit.crjdt.Tag.{ListT, MapT, RegT}

sealed trait Context extends Product with Serializable {
  def key: Tag

  // PRESENCE1, PRESENCE2
  def pres: Set[Id]

  def addId(key: Tag, id: Id, mut: Mutation): Context =
    mut match {
      // ADD-ID2
      case DeleteM => this

      // ADD-ID1
      case _ => if (key == this.key) withPres(pres + id) else this
    }

  /*
  def getChild(key: Tag): Option[Context] =
    this match {
      case MapCtx(k, _, children) if k == key => ??? // children
      case ListCtx(k,_, children) if k == key => ??? // children
        // in lists we have to perform a linear search for key
        // since the cursor just contains the Id of the element
        // (or HeadK)
      case RegCtx(k,_, _) => ???
    }
  */

  def withPres(pres: Set[Id]): Context =
    this match {
      case ctx: MapCtx => ctx.copy(pres = pres)
      case ctx: ListCtx => ctx.copy(pres = pres)
      case ctx: RegCtx => ctx.copy(pres = pres)
    }
}

object Context {
  final case class MapCtx(key: MapT,
                          pres: Set[Id],
                          children: Map[Tag, Context])
      extends Context

  final case class ListCtx(key: ListT, pres: Set[Id], children: CtxList)
      extends Context

  final case class RegCtx(key: RegT, pres: Set[Id], values: RegValues)
      extends Context

  ///

  trait CtxList extends Product with Serializable

  object CtxList {
    final case class Head(next: CtxList) extends CtxList
    final case class Node(ctx: Context, next: CtxList) extends CtxList
    case object Tail extends CtxList

    def empty: CtxList =
      Head(Tail)
  }

  ///

  def emptyDoc: Context =
    MapCtx(MapT(Key.DocK), Set.empty, Map.empty)
}
