package eu.timepit.crjdt.core
package examples

import eu.timepit.crjdt.core.Key.{DocK, IdK, StrK}
import eu.timepit.crjdt.core.ListRef.{HeadR, IdR, TailR}
import eu.timepit.crjdt.core.Node.{ListNode, MapNode, RegNode}
import eu.timepit.crjdt.core.TypeTag.{ListT, MapT, RegT}
import eu.timepit.crjdt.core.Val.Str
import eu.timepit.crjdt.core.syntax._
import org.scalacheck.Prop._
import org.scalacheck.Properties

object Figure6 extends Properties("Figure6") {
  property("internal state") = secure {
    val list = v("list")
    val eggs = v("eggs")
    val cmd = (doc := `{}`) `;`
        (let(list) = doc.downField("shopping").iter) `;`
        list.insert("eggs") `;`
        (let(eggs) = list.next) `;`
        eggs.insert("milk") `;`
        list.insert("cheese")

    val shoppingNode = ListNode(
      Map(
        RegT(IdK(Id(4, ""))) -> RegNode(Map(Id(4, "") -> Str("cheese"))),
        RegT(IdK(Id(2, ""))) -> RegNode(Map(Id(2, "") -> Str("eggs"))),
        RegT(IdK(Id(3, ""))) -> RegNode(Map(Id(3, "") -> Str("milk")))
      ),
      Map(IdK(Id(4, "")) -> Set(Id(4, "")),
          IdK(Id(2, "")) -> Set(Id(2, "")),
          IdK(Id(3, "")) -> Set(Id(3, ""))),
      Map(HeadR -> IdR(Id(4, "")),
          IdR(Id(4, "")) -> IdR(Id(2, "")),
          IdR(Id(2, "")) -> IdR(Id(3, "")),
          IdR(Id(3, "")) -> TailR)
    )

    val rootNode = MapNode(
      Map(
        MapT(DocK) -> MapNode(
          Map(ListT(StrK("shopping")) -> shoppingNode),
          Map(StrK("shopping") -> Set(Id(2, ""), Id(3, ""), Id(4, ""))))),
      Map(DocK -> Set(Id(1, ""), Id(2, ""), Id(3, ""), Id(4, "")))
    )

    Replica.empty("").applyCmd(cmd).document ?= rootNode
  }
}
