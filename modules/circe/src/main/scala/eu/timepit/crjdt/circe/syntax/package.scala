package eu.timepit.crjdt.circe

import eu.timepit.crjdt.core.Node
import eu.timepit.crjdt.core.Node.{ListNode, MapNode, RegNode}
import io.circe.Json

package object syntax extends NodeToJson {

  implicit class PimpNode(node: Node)(implicit rcr: RegNodeConflictResolver) {
    def toJson: Json =
      node match {
        case v: MapNode  => mapToJson(v)
        case v: ListNode => listToJson(v)
        case v: RegNode  => rcr.registerToJson(v)
      }
  }

}
