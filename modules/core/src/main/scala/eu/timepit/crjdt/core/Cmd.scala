package eu.timepit.crjdt.core

sealed trait Cmd extends Product with Serializable

object Cmd {
  final case class Let(x: Expr.Var, expr: Expr) extends Cmd
  final case class Assign(expr: Expr, value: Val) extends Cmd
  final case class Insert(expr: Expr, value: Val) extends Cmd
  final case class Delete(expr: Expr) extends Cmd
  final case class Sequence(cmd1: Cmd, cmd2: Cmd) extends Cmd
}
