package eu.timepit.crjdt.core

import eu.timepit.crjdt.core.Cmd._
import eu.timepit.crjdt.core.Expr._
import eu.timepit.crjdt.core.Val._

package object syntax {
  val doc: Expr = Doc
  val let: LetSyntax = new LetSyntax
  def v(name: String): Var = Var(name)
  val `{}`: BranchVal = EmptyMap
  val `[]`: BranchVal = EmptyList

  implicit final class CmdOps(val self: Cmd) extends AnyVal {
    def `;`(cmd2: Cmd): Cmd = Sequence(self, cmd2)
  }

  implicit final class ExprOps(val self: Expr) extends AnyVal {
    def :=(value: Val): Cmd = Assign(self, value)
    def insert(value: Val): Cmd = Insert(self, value)
    def delete: Cmd = Delete(self)

    def downField(key: String): Expr = DownField(self, key)
    def iter: Expr = Iter(self)
    def next: Expr = Next(self)
  }

  implicit def boolean2Val(b: Boolean): LeafVal = if (b) True else False
  implicit def string2Val(s: String): LeafVal = Str(s)
}
