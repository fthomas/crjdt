package eu.timepit.crjdt

import cats.data.State

object ReplicaStateCompanion {
  def incrementCounter: ReplicaState[BigInt] =
    State(s => (s.copy(opsCount = s.opsCount + 1), s.opsCount))

  def nextId: ReplicaState[Id] =
    incrementCounter.transform { case (s, c) => (s, Id(c, s.replicaId)) }
}
