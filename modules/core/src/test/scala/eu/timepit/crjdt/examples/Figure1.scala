package eu.timepit.crjdt
package examples

import eu.timepit.crjdt.syntax._
import org.scalacheck.Prop._
import org.scalacheck.Properties

object Figure1 extends Properties("Figure1") {
  property("convergence") = secure {
    val p0 = ReplicaState.empty("p").applyCmd(doc.downField("key") := "A")
    val q0 = p0.copy(replicaId = "q")

    // concurrent modifications

    val p1 = p0.applyCmd(doc.downField("key") := "B")
    val q1 = q0.applyCmd(doc.downField("key") := "C")

    // network communication

    //p1.context ?= q1.context
    true
  }
}
