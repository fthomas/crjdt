package eu.timepit.crjdt

import eu.timepit.crjdt.Expr.Var
import eu.timepit.crjdt.Operation.Mutation

final case class LocalState(replicaId: ReplicaId,
                            opsCounter: BigInt,
                            variables: Map[Var, Cursor],
                            processedOps: Set[Id],
                            generatedOps: Vector[Operation]) {

  def addVar(x: Var, cur: Cursor): LocalState =
    copy(variables = variables.updated(x, cur))

  // APPLY-LOCAL
  def applyLocal(op: Operation): LocalState = {
    // TODO: evaluate op to produce a modified local state
    copy(processedOps = processedOps + op.id,
         generatedOps = generatedOps :+ op)
  }

  def currentId: Id =
    Id(opsCounter, replicaId)

  // VAR
  def getVar(x: Var): Option[Cursor] =
    variables.get(x)

  def increaseCounterTo(c: BigInt): LocalState =
    if (opsCounter < c) copy(opsCounter = c) else this

  def incrementCounter: LocalState =
    copy(opsCounter = opsCounter + 1)

  // MAKE-OP
  def makeOp(cur: Cursor, mut: Mutation): LocalState = {
    val newState = incrementCounter
    val op = Operation(newState.currentId, newState.processedOps, cur, mut)
    newState.applyLocal(op)
  }
}

object LocalState {
  def empty(replicaId: ReplicaId): LocalState =
    LocalState(replicaId, 0, Map.empty, Set.empty, Vector.empty)
}
