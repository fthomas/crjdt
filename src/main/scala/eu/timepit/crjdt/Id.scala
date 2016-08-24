package eu.timepit.crjdt

/** Lamport timestamp that uniquely identifies an operation. */
final case class Id(c: BigInt, p: ReplicaId)
