package eu.timepit.crjdt.core

abstract class BeforeAfter
object Before extends BeforeAfter
object After extends BeforeAfter

sealed trait Cmd extends Product with Serializable

object Cmd {
  final case class Let(x: Expr.Var, expr: Expr) extends Cmd
  final case class Assign(expr: Expr, value: Val) extends Cmd
  final case class Insert(expr: Expr, value: Val) extends Cmd
  final case class Delete(expr: Expr) extends Cmd

  /** Moves the object at @param moveExpr before or after the object at @param targetExpr.
    * Both objects need to have the same parent, and the parent has to be of type ListT.
    * 'Before' means the moved objects new index will be one lower than the index of the object at targetExpr
    * 'After' means the moved objects new index will be one higher than the index of the object at targetExpr
    * Example: Applying a MoveVertical(3, 0, After) on a List [0123] will result in [0312].
    */
  final case class MoveVertical(moveExpr: Expr,
                                targetExpr: Expr,
                                beforeAfter: BeforeAfter)
      extends Cmd
  final case class Sequence(cmd1: Cmd, cmd2: Cmd) extends Cmd
}
