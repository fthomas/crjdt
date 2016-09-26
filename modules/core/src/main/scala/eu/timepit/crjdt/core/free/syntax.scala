package eu.timepit.crjdt.core.free

import eu.timepit.crjdt.core.Expr._
import eu.timepit.crjdt.core.Val._
import eu.timepit.crjdt.core.{Expr, Val}

object syntax {
  val doc: Expr = Doc
  val `{}`: Val = EmptyMap
  val `[]`: Val = EmptyList

  implicit final class ExprOps(val self: Expr) extends AnyVal {
    def :=(value: Val): Cmd[Unit] = Cmd.assign(self, value)
    def insert(value: Val): Cmd[Unit] = Cmd.insert(self, value)
    def delete: Cmd[Unit] = Cmd.delete(self)

    def downField(key: String): Expr = DownField(self, key)
    def iter: Expr = Iter(self)
    def next: Expr = Next(self)
  }

  implicit def boolean2Val(b: Boolean): Val = if (b) True else False
  implicit def string2Val(s: String): Val = Str(s)
}
