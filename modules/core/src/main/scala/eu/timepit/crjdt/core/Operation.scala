package eu.timepit.crjdt.core

import eu.timepit.crjdt.core.Operation.Mutation

final case class Operation(id: Id, deps: Set[Id], cur: Cursor, mut: Mutation)

object Operation {
  sealed trait Mutation extends Product with Serializable
  object Mutation {
    final case class AssignM(value: Val) extends Mutation
    final case class InsertM(value: Val) extends Mutation
    case object DeleteM extends Mutation
  }
}
