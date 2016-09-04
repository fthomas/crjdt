package eu.timepit.crjdt
package examples

import eu.timepit.crjdt.syntax._
import org.scalacheck.Prop._
import org.scalacheck.Properties

object Figure1 extends Properties("Figure1") {
  property("convergence") = secure {
    val init = doc.downField("key") := "A"
    val p0 = ReplicaState.empty("p").applyCmd(init)
    val q0 =
      ReplicaState.empty("q").copy(receivedOps = p0.generatedOps).applyRemote
    p0.context ?= q0.context

    // concurrent modifications

    val p1 = p0.applyCmd(doc.downField("key") := "B")
    val q1 = q0.applyCmd(doc.downField("key") := "C")

    // network communication

    val p2 = p1.copy(receivedOps = q1.generatedOps).applyRemote
    val q2 = q1.copy(receivedOps = p1.generatedOps).applyRemote

    p2.context ?= q2.context
  }
}
