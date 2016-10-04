package eu.timepit.crjdt.core

sealed trait Val extends Product with Serializable
sealed trait LeafVal extends Val
sealed trait BranchVal extends Val

object Val {
  final case class Num(value: BigDecimal) extends LeafVal
  final case class Str(value: String) extends LeafVal
  case object True extends LeafVal
  case object False extends LeafVal
  case object Null extends LeafVal
  case object EmptyMap extends BranchVal
  case object EmptyList extends BranchVal
}
