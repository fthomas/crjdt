package eu.timepit.crjdt.core
package examples

import eu.timepit.crjdt.core.Context.{MapCtx, RegCtx}
import eu.timepit.crjdt.core.Key.{DocK, StrK}
import eu.timepit.crjdt.core.TypeTag.{MapT, RegT}
import eu.timepit.crjdt.core.Val.Str
import eu.timepit.crjdt.core.syntax._
import org.scalacheck.Prop._
import org.scalacheck.Properties

object Figure2 extends Properties("Figure2") {
  val colors = doc.downField("colors")
  val p0 =
    ReplicaState.empty("p").applyCmd(colors.downField("blue") := "#0000ff")
  val q0 = ReplicaState.empty("q").applyRemoteOps(p0.generatedOps)

  property("initial state") = secure {
    p0.context ?= q0.context
  }

  val p1 = p0.applyCmd(colors.downField("red") := "#ff0000")
  val q1 = q0
    .applyCmd(colors := `{}`)
    .applyCmd(colors.downField("green") := "#00ff00")

  property("divergence") = secure {
    p1.context != q1.context
  }

  val p2 = p1.applyRemoteOps(q1.generatedOps)
  val q2 = q1.applyRemoteOps(p1.generatedOps)

  property("convergence") = secure {
    p2.context ?= q2.context
  }

  property("content") = secure {
    p2.context ?= MapCtx(
      Map(
        MapT(DocK) ->
          MapCtx(
            Map(
              MapT(StrK("colors")) ->
                MapCtx(Map(RegT(StrK("blue")) -> RegCtx(Map()),
                           RegT(StrK("red")) ->
                             RegCtx(Map(Id(2, "p") -> Str("#ff0000"))),
                           RegT(StrK("green")) ->
                             RegCtx(Map(Id(3, "q") -> Str("#00ff00")))),
                       Map(StrK("red") -> Set(Id(2, "p")),
                           StrK("green") -> Set(Id(3, "q"))))),
            Map(StrK("colors") -> Set(Id(2, "p"), Id(2, "q"), Id(3, "q"))))),
      Map(DocK -> Set(Id(1, "p"), Id(2, "p"), Id(2, "q"), Id(3, "q"))))
  }
}
