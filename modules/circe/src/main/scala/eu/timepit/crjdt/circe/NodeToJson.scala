package eu.timepit.crjdt.circe

import eu.timepit.crjdt.core.Node.{ListNode, MapNode, RegNode}
import eu.timepit.crjdt.core._
import io.circe.Json
import scala.annotation.tailrec

private[circe] trait NodeToJson {

  protected def mapToJson(mapNode: MapNode)(
      implicit rcr: RegNodeConflictResolver): Json =
    if (mapNode.children.contains(TypeTag.MapT(Key.DocK))) {
      mapNode.getChild(TypeTag.MapT(Key.DocK)) match {
        case node: MapNode => mapToJson(node)
        case node: ListNode => listToJson(node)
        case node: RegNode => rcr.registerToJson(node)
      }
    } else {
      val fields = mapNode.children.collect {
        case (TypeTag.MapT(Key.StrK(key)), node: MapNode)
            if mapNode.getPres(Key.StrK(key)).nonEmpty =>
          key -> mapToJson(node)
        case (TypeTag.ListT(Key.StrK(key)), node: ListNode)
            if mapNode.getPres(Key.StrK(key)).nonEmpty =>
          key -> listToJson(node)
        case (TypeTag.RegT(Key.StrK(key)), node: RegNode)
            if mapNode.getPres(Key.StrK(key)).nonEmpty =>
          key -> rcr.registerToJson(node)
      }
      Json.fromFields(fields)
    }

  protected def listToJson(listNode: ListNode)(
      implicit rcr: RegNodeConflictResolver): Json = {
    @tailrec
    def loopOrder(listRef: ListRef, keyOrder: Vector[Key]): Vector[Key] =
      listRef match {
        case keyRef: KeyRef =>
          val key = keyRef.toKey
          val next = listNode.order(keyRef)
          if (listNode.getPres(key).nonEmpty) {
            loopOrder(next, keyOrder :+ key)
          } else {
            loopOrder(next, keyOrder)
          }
        case ListRef.TailR => keyOrder
      }
    val keyOrder = loopOrder(ListRef.HeadR, Vector.empty).zipWithIndex.toMap
    val jsons = new Array[Json](keyOrder.size)
    listNode.children.foreach {
      case (TypeTag.MapT(key), node: MapNode)
          if listNode.getPres(key).nonEmpty =>
        jsons(keyOrder(key)) = mapToJson(node)
      case (TypeTag.ListT(key), node: ListNode)
          if listNode.getPres(key).nonEmpty =>
        jsons(keyOrder(key)) = listToJson(node)
      case (TypeTag.RegT(key), node: RegNode)
          if listNode.getPres(key).nonEmpty =>
        jsons(keyOrder(key)) = rcr.registerToJson(node)
    }
    Json.fromValues(jsons)
  }

}
