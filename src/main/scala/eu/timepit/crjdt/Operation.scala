package eu.timepit.crjdt

import eu.timepit.crjdt.Operation.Mutation

final case class Operation(id: Id, deps: Set[Id], cur: Cursor, mut: Mutation)

object Operation {
  sealed trait Mutation extends Product with Serializable
  object Mutation {
    final case class Assign(v: Val) extends Mutation
    final case class Insert(v: Val) extends Mutation
    case object Delete extends Mutation
  }
}
