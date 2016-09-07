package eu.timepit.crjdt.core

sealed trait Val extends Product with Serializable

object Val {
  final case class Num(value: BigDecimal) extends Val
  final case class Str(value: String) extends Val
  case object True extends Val
  case object False extends Val
  case object Null extends Val
  case object EmptyMap extends Val
  case object EmptyList extends Val
}
