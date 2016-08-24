package eu.timepit.crjdt

import cats.data.State
import eu.timepit.crjdt.Expr.Var

object ReplicaStateCompanion {
  // VAR
  def getVar(v: Var): ReplicaState[Option[Cursor]] =
    State.inspect(_.variables.get(v))

  def increaseCounterTo(c: BigInt): ReplicaState[Unit] =
    State.modify(s => if (s.opsCount < c) s.copy(opsCount = c) else s)

  def incrementCounter: ReplicaState[BigInt] =
    State(s => (s.copy(opsCount = s.opsCount + 1), s.opsCount))

  def nextId: ReplicaState[Id] =
    incrementCounter.transform { case (s, c) => (s, Id(c, s.replicaId)) }
}
