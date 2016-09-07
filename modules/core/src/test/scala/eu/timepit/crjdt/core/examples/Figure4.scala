package eu.timepit.crjdt.core
package examples

import eu.timepit.crjdt.core.syntax._
import org.scalacheck.Prop._
import org.scalacheck.Properties

object Figure4 extends Properties("Figure4") {
  val todo = doc.downField("todo").iter
  val cmd = (todo.downField("title") := "buy milk") `;` (todo.downField("done") := false)

  val p0 = ReplicaState.empty("p").applyCmd(cmd)
  val q0 = ReplicaState.empty("q").applyRemoteOps(p0.generatedOps)

  property("initial state") = secure {
    p0.context ?= q0.context
  }

  val p1 = p0.applyCmd(todo.iter.next.delete)
  val q1 = q0.applyCmd(todo.iter.next.downField("done") := true)

  property("divergence") = secure {
    p1.context != q1.context
  }

  val p2 = p1.applyRemoteOps(q1.generatedOps)
  val q2 = q1.applyRemoteOps(p1.generatedOps)

  property("convergence") = secure {
    p2.context ?= q2.context
  }
}
