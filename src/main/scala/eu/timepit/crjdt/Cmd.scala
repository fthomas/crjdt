package eu.timepit.crjdt

sealed trait Cmd extends Product with Serializable

object Cmd {

  /** Sets the value of a local variable. */
  final case class Let(x: Expr.Var, expr: Expr) extends Cmd

  /** Assigns the value of a register. */
  final case class Assign(expr: Expr, v: Val) extends Cmd

  /** Inserts an element into a list. */
  final case class Insert(expr: Expr, v: Val) extends Cmd

  /** Deletes an element from a list or map. */
  final case class Delete(expr: Expr) extends Cmd

  /** Performs network communication. */
  case object Yield extends Cmd

  final case class Sequence(cmd1: Cmd, cmd2: Cmd) extends Cmd
}
