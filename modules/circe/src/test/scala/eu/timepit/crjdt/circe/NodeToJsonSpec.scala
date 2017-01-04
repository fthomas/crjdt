package eu.timepit.crjdt.circe

import eu.timepit.crjdt.core.Replica
import eu.timepit.crjdt.core.syntax._
import eu.timepit.crjdt.circe.testUtil._
import io.circe._
import io.circe.testing.ArbitraryInstances
import org.scalacheck.Properties
import org.scalacheck.Prop._
import eu.timepit.crjdt.circe.syntax._
import eu.timepit.crjdt.circe.RegNodeConflictResolver.LWW

object NodeToJsonSpec
    extends Properties("NodeToJsonSpec")
    with ArbitraryInstances {

  // filter out numbers BigDecimal (and Val.Num) cannot handle
  override def transformJsonNumber(n: JsonNumber): JsonNumber =
    if (n.toDouble == -0.0 || n.toDouble == Double.PositiveInfinity || n.toDouble == Double.NegativeInfinity) {
      JsonNumber.fromDecimalStringUnsafe("0.0")
    } else n

  property("toJson") = forAllNoShrink { (obj: JsonObject) =>
    val json = Json.fromJsonObject(obj)
    val commands = assignObjectFieldsCmds(doc, json.asObject.get)
    val document = Replica.empty("").applyCmds(commands.toList).document
    document.toJson ?= json
  }

}
