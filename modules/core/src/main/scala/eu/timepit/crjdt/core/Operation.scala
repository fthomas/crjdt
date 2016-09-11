package eu.timepit.crjdt.core

final case class Operation(id: Id, deps: Set[Id], cur: Cursor, mut: Mutation)
