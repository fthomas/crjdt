import Expr._

object syntax {
  def doc: Expr = Doc

  implicit class ExprOps(val self: Expr) extends AnyVal {
    def downField(key: String): Expr = DownField(self, key)
    def iter: Expr = Iter(self)
    def next: Expr = Next(self)
    def keys: Expr = Keys(self)
    def values: Expr = Values(self)
  }
}
