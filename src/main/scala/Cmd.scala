sealed trait Cmd extends Product with Serializable

object Cmd {
  final case class Let(x: Expr.Var, expr: Expr) extends Cmd

  final case class Assign(expr: Expr, v: Val) extends Cmd

  /** Inserts an element into a list. */
  final case class Insert(expr: Expr, v: Val) extends Cmd

  /** Deletes an element from a list or map. */
  final case class Delete(expr: Expr) extends Cmd

  case object Yield extends Cmd

  final case class Sequence(cmd1: Cmd, cmd2: Cmd) extends Cmd
}
