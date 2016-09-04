package eu.timepit.crjdt
package examples

import eu.timepit.crjdt.syntax._
import org.scalacheck.Prop._
import org.scalacheck.Properties

object Figure2 extends Properties("Figure2") {
  val colors = doc.downField("colors")
  val p0 =
    ReplicaState.empty("p").applyCmd(colors.downField("blue") := "#0000ff")
  val q0 =
    ReplicaState.empty("q").copy(receivedOps = p0.generatedOps).applyRemote

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

  val p2 = p1.copy(receivedOps = q1.generatedOps).applyRemote.applyRemote
  val q2 = q1.copy(receivedOps = p1.generatedOps).applyRemote

  property("convergence") = secure {
    p2.context ?= q2.context
  }
}
