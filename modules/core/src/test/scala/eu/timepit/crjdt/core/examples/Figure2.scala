package eu.timepit.crjdt.core
package examples

import eu.timepit.crjdt.core.Key.{DocK, StrK}
import eu.timepit.crjdt.core.Node.{MapNode, RegNode}
import eu.timepit.crjdt.core.TypeTag.{MapT, RegT}
import eu.timepit.crjdt.core.Val.Str
import eu.timepit.crjdt.core.syntax._
import eu.timepit.crjdt.core.testUtil._
import org.scalacheck.Prop._
import org.scalacheck.Properties

object Figure2 extends Properties("Figure2") {
  val colors = doc.downField("colors")
  val p0 =
    Replica.empty("p").applyCmd(colors.downField("blue") := "#0000ff")
  val q0 = merge(Replica.empty("q"), p0)

  property("initial state") = secure {
    converged(p0, q0)
  }

  val p1 = p0.applyCmd(colors.downField("red") := "#ff0000")
  val q1 = q0
    .applyCmd(colors := `{}`)
    .applyCmd(colors.downField("green") := "#00ff00")

  property("divergence") = secure {
    diverged(p1, q1)
  }

  val p2 = merge(p1, q1)
  val q2 = merge(q1, p1)

  property("convergence") = secure {
    converged(p2, q2)
  }

  property("content") = secure {
    p2.document ?= MapNode(
      Map(
        MapT(DocK) ->
          MapNode(
            Map(
              MapT(StrK("colors")) ->
                MapNode(Map(RegT(StrK("blue")) -> RegNode(Map()),
                            RegT(StrK("red")) ->
                              RegNode(Map(Id(2, "p") -> Str("#ff0000"))),
                            RegT(StrK("green")) ->
                              RegNode(Map(Id(3, "q") -> Str("#00ff00")))),
                        Map(StrK("red") -> Set(Id(2, "p")),
                            StrK("green") -> Set(Id(3, "q"))))),
            Map(StrK("colors") -> Set(Id(2, "p"), Id(2, "q"), Id(3, "q"))))),
      Map(DocK -> Set(Id(1, "p"), Id(2, "p"), Id(2, "q"), Id(3, "q"))))
  }

  property("keys") = secure {
    (p2.keys(doc) ?= Set("colors")) &&
    (p2.keys(colors) ?= Set("red", "green"))
  }
}
