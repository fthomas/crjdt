package eu.timepit.crjdt

import eu.timepit.crjdt.Tag.{ListT, MapT, RegT}

sealed trait Context extends Product with Serializable

object Context {
  final case class MapCtx(key: MapT,
                          pres: Set[Id],
                          children: Map[Tag, Context])
      extends Context

  final case class ListCtx(key: ListT, pres: Set[Id], children: CtxList)

  final case class RegCtx(key: RegT, pres: Set[Id], values: RegValues)
      extends Context

  ///

  trait CtxList extends Product with Serializable

  object CtxList {
    final case class Head(next: CtxList) extends CtxList
    final case class Node(ctx: Context, next: CtxList) extends CtxList
    case object Tail extends CtxList
  }

  ///

  def empty: Context =
    MapCtx(MapT(Key.DocK), Set.empty, Map.empty)
}
