package eu.timepit.crjdt.circe

import catalysts.Platform
import eu.timepit.crjdt.core._
import eu.timepit.crjdt.core.syntax._
import eu.timepit.crjdt.circe.testUtil._
import io.circe._
import io.circe.testing.ArbitraryInstances
import org.scalacheck.Properties
import org.scalacheck.Prop._
import eu.timepit.crjdt.circe.syntax._
import eu.timepit.crjdt.circe.RegNodeConflictResolver.LWW
import eu.timepit.crjdt.core.Key.StrK
import eu.timepit.crjdt.core.Node.MapNode

object NodeToJsonSpec
    extends Properties("NodeToJsonSpec")
    with ArbitraryInstances {

  // avoid Scala.js test failure at travis CI
  override protected def maxJsonArraySize: Int = if (Platform.isJs) 3 else 10

  override protected def maxJsonDepth: Int = if (Platform.isJs) 2 else 5

  override protected def maxJsonObjectSize: Int = if (Platform.isJs) 1 else 10

  // filter out numbers BigDecimal (and Val.Num) cannot handle
  override def transformJsonNumber(n: JsonNumber): JsonNumber =
    if (n.toDouble == -0.0 || n.toDouble == Double.PositiveInfinity || n.toDouble == Double.NegativeInfinity) {
      JsonNumber.fromDecimalStringUnsafe("0.0")
    } else n

  property("root document to JSON") = forAllNoShrink { (obj: JsonObject) =>
    val json = Json.fromJsonObject(obj)
    val commands = assignObjectFieldsCmds(doc, obj)
    val document = Replica.empty("").applyCmds(commands.toList).document
    document.toJson ?= json
  }

  property("Node including ListNode and RegNode to JSON") = forAllNoShrink {
    (obj: JsonObject) =>
      val commands = assignObjectFieldsCmds(doc, obj)
      val document = Replica.empty("").applyCmds(commands.toList).document
      val childNodes: Map[String, Node] = document
        .findChild(TypeTag.MapT(Key.DocK))
        .collect {
          case node: MapNode =>
            node.children.collect {
              case (TypeTag.MapT(StrK(key)), value)  => (key, value)
              case (TypeTag.ListT(StrK(key)), value) => (key, value)
              case (TypeTag.RegT(StrK(key)), value)  => (key, value)
            }
        }
        .getOrElse(Map.empty)
      childNodes.mapValues(_.toJson) ?= obj.toMap
  }

}
