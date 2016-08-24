package eu.timepit

import cats.data.State

package object crjdt {
  type ReplicaId = String

  type ReplicaState[A] = State[LocalState, A]

  // workaround for https://issues.scala-lang.org/browse/SI-7139
  val ReplicaState: ReplicaStateCompanion.type = ReplicaStateCompanion
}
