package eu.timepit.crjdt.circe

import eu.timepit.crjdt.core._
import eu.timepit.crjdt.core.syntax._
import eu.timepit.crjdt.circe.testUtil._
import io.circe._
import io.circe.testing.ArbitraryInstances
import org.scalacheck.Properties
import org.scalacheck.Prop._
import eu.timepit.crjdt.circe.syntax._
import eu.timepit.crjdt.circe.RegNodeConflictResolver.LWW
import eu.timepit.crjdt.core.Key.IdK
import eu.timepit.crjdt.core.ListRef.{HeadR, IdR, TailR}
import eu.timepit.crjdt.core.Node.{ListNode, RegNode}
import eu.timepit.crjdt.core.TypeTag.RegT
import eu.timepit.crjdt.core.Val.Str

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

  property("ListNode to JSON") = secure {
    val groceryList = ListNode(
      Map(RegT(IdK(Id(2, "q"))) -> RegNode(Map(Id(2, "q") -> Str("milk"))),
          RegT(IdK(Id(3, "q"))) -> RegNode(Map(Id(3, "q") -> Str("flour"))),
          RegT(IdK(Id(2, "p"))) -> RegNode(Map(Id(2, "p") -> Str("eggs"))),
          RegT(IdK(Id(3, "p"))) -> RegNode(Map(Id(3, "p") -> Str("ham")))),
      Map(IdK(Id(2, "p")) -> Set(Id(2, "p")),
          IdK(Id(3, "p")) -> Set(Id(3, "p")),
          IdK(Id(2, "q")) -> Set(Id(2, "q")),
          IdK(Id(3, "q")) -> Set(Id(3, "q"))),
      Map(HeadR -> IdR(Id(2, "q")),
          IdR(Id(2, "q")) -> IdR(Id(3, "q")),
          IdR(Id(3, "q")) -> IdR(Id(2, "p")),
          IdR(Id(2, "p")) -> IdR(Id(3, "p")),
          IdR(Id(3, "p")) -> TailR))

    groceryList.toJson ?= Json.arr(
      List("milk", "flour", "eggs", "ham").map(Json.fromString): _*)
  }

  property("RegNode to JSON") = secure {
    RegNode(Map(Id(1, "p") -> Val.True)).toJson ?= Json.True
  }

}
