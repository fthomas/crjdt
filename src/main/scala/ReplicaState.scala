final case class ReplicaState(opsCount: BigInt, replicaId: String) {
  def incrementOpsCount: ReplicaState =
    copy(opsCount = opsCount + 1)
}

object ReplicaState {
  def empty(replicaId: String): ReplicaState =
    ReplicaState(opsCount = 0, replicaId = replicaId)
}
