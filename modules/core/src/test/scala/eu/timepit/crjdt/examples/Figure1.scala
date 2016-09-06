package eu.timepit.crjdt
package examples

import eu.timepit.crjdt.syntax._
import org.scalacheck.Prop._
import org.scalacheck.Properties

object Figure1 extends Properties("Figure1") {
  val p0 = ReplicaState.empty("p").applyCmd(doc.downField("key") := "A")
  val q0 = ReplicaState.empty("q").applyRemoteOps(p0.generatedOps)

  property("initial state") = secure {
    p0.context ?= q0.context
  }

  val p1 = p0.applyCmd(doc.downField("key") := "B")
  val q1 = q0.applyCmd(doc.downField("key") := "C")

  property("divergence") = secure {
    p1.context != q1.context
  }

  val p2 = p1.applyRemoteOps(q1.generatedOps)
  val q2 = q1.applyRemoteOps(p1.generatedOps)

  property("convergence") = secure {
    p2.context ?= q2.context
  }
}
