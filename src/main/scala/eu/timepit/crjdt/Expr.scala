package eu.timepit.crjdt

sealed trait Expr extends Product with Serializable

object Expr {
  case object Doc extends Expr
  final case class Var(name: String) extends Expr
  final case class DownField(expr: Expr, key: String) extends Expr
  final case class Iter(expr: Expr) extends Expr
  final case class Next(expr: Expr) extends Expr
  final case class Keys(expr: Expr) extends Expr
  final case class Values(expr: Expr) extends Expr
}
