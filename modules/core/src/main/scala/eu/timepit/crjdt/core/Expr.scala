package eu.timepit.crjdt.core

/** This corresponds to the expression construct `EXPR` in Kleppmann
  * and Beresford. It is used to construct a `[[Cursor]]`.
  *
  * In contrast to the paper, `Expr` does not include the `keys` and
  * `values` operations which can be used to query the state of a
  * document. This simplifies the evaluation of an `Expr` which always
  * yields a `[[Cursor]]`.
  */
sealed trait Expr extends Product with Serializable

object Expr {
  case object Doc extends Expr
  final case class Var(name: String) extends Expr
  final case class DownField(expr: Expr, key: String) extends Expr
  final case class Iter(expr: Expr) extends Expr
  final case class Next(expr: Expr) extends Expr
}
