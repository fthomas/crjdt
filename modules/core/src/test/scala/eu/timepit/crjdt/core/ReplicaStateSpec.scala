package eu.timepit.crjdt.core

import eu.timepit.crjdt.core.arbitrary._
import org.scalacheck.Prop._
import org.scalacheck.Properties

object ReplicaStateSpec extends Properties("ReplicaState") {
  property("convergence 1") = forAll { (cmds: List[Cmd]) =>
    val p = ReplicaState.empty("p").applyCmds(cmds)
    val q = ReplicaState.empty("q")

    val ops = p.generatedOps
    p.context ?= q.applyRemoteOps(ops).context
  }

  property("convergence 2") = forAll { (cmds1: List[Cmd], cmds2: List[Cmd]) =>
    val p0 = ReplicaState.empty("p").applyCmds(cmds1)
    val q0 = ReplicaState.empty("q").applyCmds(cmds2)

    val p1 = p0.applyRemoteOps(q0.generatedOps)
    val q1 = q0.applyRemoteOps(p0.generatedOps)

    p1.context ?= q1.context
  }

  property("convergence 3") = forAll {
    (cmds1: List[Cmd], cmds2: List[Cmd], cmds3: List[Cmd]) =>
      val p0 = ReplicaState.empty("p").applyCmds(cmds1)
      val q0 = ReplicaState.empty("q").applyCmds(cmds2)
      val r0 = ReplicaState.empty("r").applyCmds(cmds3)

      val p1 = p0.applyRemoteOps(q0.generatedOps ++ r0.generatedOps)
      val q1 = q0.applyRemoteOps(p0.generatedOps ++ r0.generatedOps)
      val r1 = r0.applyRemoteOps(p0.generatedOps ++ q0.generatedOps)

      (p1.context ?= q1.context) && (q1.context ?= r1.context)
  }

  property("commutativity") = forAll { (cmds: List[Cmd]) =>
    val p = ReplicaState.empty("p").applyCmds(cmds)
    val q = ReplicaState.empty("q")

    // If we check all permutations, this test runs forever.
    p.generatedOps.permutations.take(5).forall { ops =>
      p.context == q.applyRemoteOps(ops).context
    }
  }
}
