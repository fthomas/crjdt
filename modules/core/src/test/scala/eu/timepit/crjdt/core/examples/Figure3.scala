package eu.timepit.crjdt.core
package examples

import eu.timepit.crjdt.core.Key.{DocK, IdK, StrK}
import eu.timepit.crjdt.core.ListRef.{HeadR, IdR, TailR}
import eu.timepit.crjdt.core.Node.{ListNode, MapNode, RegNode}
import eu.timepit.crjdt.core.TypeTag.{ListT, MapT, RegT}
import eu.timepit.crjdt.core.Val.Str
import eu.timepit.crjdt.core.syntax._
import eu.timepit.crjdt.core.testUtil._
import org.scalacheck.Prop._
import org.scalacheck.Properties

object Figure3 extends Properties("Figure3") {
  val p0 = Replica.empty("p")
  val q0 = Replica.empty("q")

  property("initial state") = secure {
    converged(p0, q0)
  }

  val grocery = doc.downField("grocery")

  val p1 = p0
    .applyCmd(grocery := `[]`)
    .applyCmd(grocery.iter.insert("eggs"))
    .applyCmd(grocery.iter.next.insert("ham"))
  val q1 = q0
    .applyCmd(grocery := `[]`)
    .applyCmd(grocery.iter.insert("milk"))
    .applyCmd(grocery.iter.next.insert("flour"))

  property("divergence") = secure {
    diverged(p1, q1)
  }

  val p2 = merge(p1, q1)
  val q2 = merge(q1, p1)

  property("convergence") = secure {
    converged(p2, q2)
  }

  property("content") = secure {
    val groceryList = ListNode(
      Map(
        RegT(IdK(Id(2, "q"))) -> RegNode(Map(Id(2, "q") -> Str("milk"))),
        RegT(IdK(Id(3, "q"))) -> RegNode(Map(Id(3, "q") -> Str("flour"))),
        RegT(IdK(Id(2, "p"))) -> RegNode(Map(Id(2, "p") -> Str("eggs"))),
        RegT(IdK(Id(3, "p"))) -> RegNode(Map(Id(3, "p") -> Str("ham")))
      ),
      Map(IdK(Id(2, "p")) -> Set(Id(2, "p")),
          IdK(Id(3, "p")) -> Set(Id(3, "p")),
          IdK(Id(2, "q")) -> Set(Id(2, "q")),
          IdK(Id(3, "q")) -> Set(Id(3, "q"))),
      Map(HeadR -> IdR(Id(2, "q")),
          IdR(Id(2, "q")) -> IdR(Id(3, "q")),
          IdR(Id(3, "q")) -> IdR(Id(2, "p")),
          IdR(Id(2, "p")) -> IdR(Id(3, "p")),
          IdR(Id(3, "p")) -> TailR),
      Map()
    )
    val pres = Set(Id(2, "p"),
                   Id(2, "q"),
                   Id(1, "q"),
                   Id(3, "q"),
                   Id(3, "p"),
                   Id(1, "p"))

    p2.document ?= MapNode(
      Map(
        MapT(DocK) -> MapNode(Map(ListT(StrK("grocery")) -> groceryList),
                              Map(StrK("grocery") -> pres))),
      Map(DocK -> pres))
  }
}
