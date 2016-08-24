package eu.timepit.crjdt

import eu.timepit.crjdt.Expr.Var

final case class LocalState(opsCount: BigInt,
                            replicaId: ReplicaId,
                            variables: Map[Var, Cursor])
