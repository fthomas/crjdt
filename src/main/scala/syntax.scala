import Cmd._
import Expr._
import Val.{EmptyArray, EmptyObject}

object syntax {
  class LetSyntax {
    def update(x: Var, expr: Expr): Cmd = Let(x, expr)
  }

  def doc: Expr = Doc
  def let: LetSyntax = new LetSyntax
  def v(x: String): Var = Var(x)
  def `{}`: Val = EmptyObject
  def `[]`: Val = EmptyArray

  implicit class ExprOps(val self: Expr) extends AnyVal {
    def :=(v: Val): Cmd = Assign(self, v)
    def insert(v: Val): Cmd = Insert(self, v)
    def delete: Cmd = Delete(self)

    def downField(key: String): Expr = DownField(self, key)
    def iter: Expr = Iter(self)
    def next: Expr = Next(self)
    def keys: Expr = Keys(self)
    def values: Expr = Values(self)
  }
}
