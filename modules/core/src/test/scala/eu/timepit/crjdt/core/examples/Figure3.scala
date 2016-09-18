package eu.timepit.crjdt.core
package examples

import eu.timepit.crjdt.core.Context.{ListCtx, MapCtx, RegCtx}
import eu.timepit.crjdt.core.Key.{DocK, IdK, StrK}
import eu.timepit.crjdt.core.ListRef.{HeadR, IdR, TailR}
import eu.timepit.crjdt.core.TypeTag.{ListT, MapT, RegT}
import eu.timepit.crjdt.core.Val.Str
import eu.timepit.crjdt.core.syntax._
import org.scalacheck.Prop._
import org.scalacheck.Properties

object Figure3 extends Properties("Figure3") {
  val p0 = ReplicaState.empty("p")
  val q0 = ReplicaState.empty("q")

  property("initial state") = secure {
    p0.context ?= q0.context
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
    p1.context != q1.context
  }

  val p2 = p1.applyRemoteOps(q1.generatedOps)
  val q2 = q1.applyRemoteOps(p1.generatedOps)

  property("convergence") = secure {
    p2.context ?= q2.context
  }

  property("content") = secure {
    val groceryList = ListCtx(
      Map(RegT(IdK(Id(2, "q"))) -> RegCtx(Map(Id(2, "q") -> Str("milk"))),
          RegT(IdK(Id(3, "q"))) -> RegCtx(Map(Id(3, "q") -> Str("flour"))),
          RegT(IdK(Id(2, "p"))) -> RegCtx(Map(Id(2, "p") -> Str("eggs"))),
          RegT(IdK(Id(3, "p"))) -> RegCtx(Map(Id(3, "p") -> Str("ham")))),
      Map(IdK(Id(2, "p")) -> Set(Id(2, "p")),
          IdK(Id(3, "p")) -> Set(Id(3, "p")),
          IdK(Id(2, "q")) -> Set(Id(2, "q")),
          IdK(Id(3, "q")) -> Set(Id(3, "q"))),
      Map(HeadR -> IdR(Id(2, "q")),
          IdR(Id(2, "q")) -> IdR(Id(3, "q")),
          IdR(Id(3, "q")) -> IdR(Id(2, "p")),
          IdR(Id(2, "p")) -> IdR(Id(3, "p")),
          IdR(Id(3, "p")) -> TailR))
    val pres = Set(Id(2, "p"),
                   Id(2, "q"),
                   Id(1, "q"),
                   Id(3, "q"),
                   Id(3, "p"),
                   Id(1, "p"))

    p2.context ?= MapCtx(
      Map(
        MapT(DocK) -> MapCtx(Map(ListT(StrK("grocery")) -> groceryList),
                             Map(StrK("grocery") -> pres))),
      Map(DocK -> pres))
  }
}
