package eu.timepit.crjdt.core

import eu.timepit.crjdt.core.Cmd.{Assign, Delete}
import eu.timepit.crjdt.core.arbitrary._
import eu.timepit.crjdt.core.testUtil._
import org.scalacheck.Prop._
import org.scalacheck.Properties

object ReplicaStateSpec extends Properties("ReplicaState") {
  val p0 = ReplicaState.empty("p")
  val q0 = ReplicaState.empty("q")
  val r0 = ReplicaState.empty("r")

  property("convergence 1") = forAll { (cmds: List[Cmd]) =>
    val p1 = p0.applyCmds(cmds)
    val q1 = merge(q0, p1)

    converged(p1, q1)
  }

  property("convergence 2") = forAll { (cmds1: List[Cmd], cmds2: List[Cmd]) =>
    val p1 = p0.applyCmds(cmds1)
    val q1 = q0.applyCmds(cmds2)

    val p2 = merge(p1, q1)
    val q2 = merge(q1, p1)

    converged(p2, q2)
  }

  property("convergence 3") = forAll {
    (cmds1: List[Cmd], cmds2: List[Cmd], cmds3: List[Cmd]) =>
      val p1 = p0.applyCmds(cmds1)
      val q1 = q0.applyCmds(cmds2)
      val r1 = r0.applyCmds(cmds3)

      val p2 = merge(merge(p1, q1), r1)
      val q2 = merge(merge(q1, p1), r1)
      val r2 = merge(merge(r1, p1), q1)

      converged(p2, q2, r2)
  }

  property("commutativity 1") = forAll { (cmds: List[Cmd]) =>
    val p1 = p0.applyCmds(cmds)
    val q1 = q0.applyRemoteOps(randomPermutation(p1.generatedOps))

    converged(p1, q1)
  }

  property("commutativity 2") = forAll {
    (cmds1: List[Cmd], cmds2: List[Cmd]) =>
      val p1 = p0.applyCmds(cmds1)
      val q1 = q0.applyCmds(cmds2)

      val p2 = p1.applyRemoteOps(randomPermutation(q1.generatedOps))
      val q2 = q1.applyRemoteOps(randomPermutation(p1.generatedOps))

      converged(p2, q2)
  }

  property("commutativity 3") = forAll {
    (cmds1: List[Cmd], cmds2: List[Cmd], cmds3: List[Cmd]) =>
      val p1 = p0.applyCmds(cmds1)
      val q1 = q0.applyCmds(cmds2)
      val r1 = r0.applyCmds(cmds3)

      val p2 = p1.applyRemoteOps(
        randomPermutation(q1.generatedOps ++ r1.generatedOps))
      val q2 = q1.applyRemoteOps(
        randomPermutation(p1.generatedOps ++ r1.generatedOps))
      val r2 = r1.applyRemoteOps(
        randomPermutation(p1.generatedOps ++ q1.generatedOps))

      converged(p2, q2, r2)
  }

  property("idempotence") = forAll { (cmd: Cmd) =>
    val p1 = p0.applyCmd(cmd)
    val q1 = merge(q0, p1)
    val q2 = merge(q1, p1)

    converged(p1, q1, q2)
  }

  property("Lemma 6") = forAll { (delete: Delete, cmd: Cmd) =>
    val p1 = p0.applyCmd(delete)
    val q1 = q0.applyCmd(cmd)

    val p2 = merge(p1, q1)
    val q2 = merge(q1, p1)

    converged(p2, q2)
  }

  property("Lemma 7") = forAll { (assign: Assign, cmd: Cmd) =>
    val p1 = p0.applyCmd(assign)
    val q1 = q0.applyCmd(cmd)

    val p2 = merge(p1, q1)
    val q2 = merge(q1, p1)

    converged(p2, q2)
  }
}
