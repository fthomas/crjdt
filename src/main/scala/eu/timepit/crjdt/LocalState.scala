package eu.timepit.crjdt

import eu.timepit.crjdt.Expr.Var

final case class LocalState(opsCount: BigInt,
                            replicaId: ReplicaId,
                            variables: Map[Var, Cursor]) {

  def addVar(x: Var, cur: Cursor): LocalState =
    copy(variables = variables.updated(x, cur))

  // VAR
  def getVar(x: Var): Option[Cursor] =
    variables.get(x)

  def increaseCounterTo(c: BigInt): LocalState =
    if (opsCount < c) copy(opsCount = c) else this
}
