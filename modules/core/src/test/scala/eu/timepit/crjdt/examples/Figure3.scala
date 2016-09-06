package eu.timepit.crjdt
package examples

import org.scalacheck.Prop._
import org.scalacheck.Properties
import syntax._

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
}
