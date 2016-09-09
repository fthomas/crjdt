package eu.timepit.crjdt.core

import eu.timepit.crjdt.core.arbitrary._
import org.scalacheck.Prop._
import org.scalacheck.Properties

object ReplicaStateSpec extends Properties("ReplicaState") {
  property("applyRemoteOps: convergence") = forAll { (cmds: List[Cmd]) =>
    val p = ReplicaState.empty("p").applyCmds(cmds)
    val q = ReplicaState.empty("q")

    val ops = p.generatedOps
    p.context ?= q.applyRemoteOps(ops).context
  }

  property("applyRemoteOps: commutativity") = forAll { (cmds: List[Cmd]) =>
    val p = ReplicaState.empty("p").applyCmds(cmds)
    val q = ReplicaState.empty("q")

    // If we check all permutations, this test runs forever.
    p.generatedOps.permutations.take(5).forall { ops =>
      p.context == q.applyRemoteOps(ops).context
    }
  }
}
