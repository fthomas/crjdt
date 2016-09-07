package eu.timepit.crjdt.core
package syntax

final class LetSyntax {
  def update(x: Expr.Var, expr: Expr): Cmd = Cmd.Let(x, expr)
}
