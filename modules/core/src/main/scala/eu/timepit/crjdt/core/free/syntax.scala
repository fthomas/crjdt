package eu.timepit.crjdt.core.free

import eu.timepit.crjdt.core.Val._
import eu.timepit.crjdt.core.{BranchVal, Cursor, LeafVal, Val}

object syntax {
  val doc: Cursor = Cursor.doc
  val `{}`: BranchVal = EmptyMap
  val `[]`: BranchVal = EmptyList

  implicit def boolean2Val(b: Boolean): LeafVal = if (b) True else False
  implicit def string2Val(s: String): LeafVal = Str(s)

  implicit final class CursorOps(val self: Cursor) extends AnyVal {
    def :=(value: Val): Cmd[Unit] =
      Cmd.assign(self, value)

    def insert(value: Val): Cmd[Unit] =
      Cmd.insert(self, value)

    def delete: Cmd[Unit] =
      Cmd.delete(self)

    def downField(key: String): Cmd[Cursor] =
      Cmd.downField(self, key)

    def iter: Cmd[Cursor] =
      Cmd.iter(self)

    def next: Cmd[Cursor] =
      Cmd.next(self)

    def keys: Cmd[Set[String]] =
      Cmd.keys(self)

    def values: Cmd[List[LeafVal]] =
      Cmd.values(self)
  }

  implicit final class CmdCursorOps(val self: Cmd[Cursor]) extends AnyVal {
    def :=(value: Val): Cmd[Unit] =
      self.flatMap(_ := value)

    def insert(value: Val): Cmd[Unit] =
      self.flatMap(_.insert(value))

    def delete: Cmd[Unit] =
      self.flatMap(_.delete)

    def downField(key: String): Cmd[Cursor] =
      self.flatMap(_.downField(key))

    def iter: Cmd[Cursor] =
      self.flatMap(_.iter)

    def next: Cmd[Cursor] =
      self.flatMap(_.next)

    def values: Cmd[List[LeafVal]] =
      self.flatMap(_.values)
  }
}
