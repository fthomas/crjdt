package eu.timepit.crjdt.core

sealed trait Mutation extends Product with Serializable

object Mutation {
  final case class AssignM(value: Val) extends Mutation
  final case class InsertM(value: Val) extends Mutation
  case object DeleteM extends Mutation
}
