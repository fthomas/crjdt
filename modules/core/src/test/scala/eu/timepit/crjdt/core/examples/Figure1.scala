package eu.timepit.crjdt.core
package examples

import eu.timepit.crjdt.core.Context.{MapCtx, RegCtx}
import eu.timepit.crjdt.core.Key.{DocK, StrK}
import eu.timepit.crjdt.core.TypeTag.{MapT, RegT}
import eu.timepit.crjdt.core.Val.Str
import eu.timepit.crjdt.core.syntax._
import eu.timepit.crjdt.core.testUtil._
import org.scalacheck.Prop._
import org.scalacheck.Properties

object Figure1 extends Properties("Figure1") {
  val p0 = ReplicaState.empty("p").applyCmd(doc.downField("key") := "A")
  val q0 = merge(ReplicaState.empty("q"), p0)

  property("initial state") = secure {
    converged(p0, q0)
  }

  val p1 = p0.applyCmd(doc.downField("key") := "B")
  val q1 = q0.applyCmd(doc.downField("key") := "C")

  property("divergence") = secure {
    diverged(p1, q1)
  }

  val p2 = merge(p1, q1)
  val q2 = merge(q1, p1)

  property("convergence") = secure {
    converged(p2, q2)
  }

  property("content") = secure {
    p2.context ?= MapCtx(
      Map(
        MapT(DocK) -> MapCtx(
          Map(RegT(StrK("key")) ->
            RegCtx(Map(Id(2, "p") -> Str("B"), Id(2, "q") -> Str("C")))),
          Map(StrK("key") -> Set(Id(1, "p"), Id(2, "p"), Id(2, "q"))))),
      Map(DocK -> Set(Id(1, "p"), Id(2, "p"), Id(2, "q"))))
  }

  property("keys") = secure {
    p2.keys(doc) ?= Set("key")
  }
}
