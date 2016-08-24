package eu.timepit.crjdt

final case class ReplicaState(opsCount: BigInt, replicaId: ReplicaId) {
  def incrementOpsCount: ReplicaState =
    copy(opsCount = opsCount + 1)
}

object ReplicaState {
  def empty(replicaId: ReplicaId): ReplicaState =
    ReplicaState(opsCount = 0, replicaId = replicaId)
}
