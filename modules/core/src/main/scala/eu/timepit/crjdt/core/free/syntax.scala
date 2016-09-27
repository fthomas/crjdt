package eu.timepit.crjdt.core.free

import eu.timepit.crjdt.core.Val._
import eu.timepit.crjdt.core.{Cursor, Val}

object syntax {
  val doc: Cmd[Cursor] = Cmd.doc
  val `{}`: Val = EmptyMap
  val `[]`: Val = EmptyList

  implicit final class CursorOps(val self: Cmd[Cursor]) extends AnyVal {
    def :=(value: Val): Cmd[Unit] =
      self.flatMap(Cmd.assign(_, value))

    def insert(value: Val): Cmd[Unit] =
      self.flatMap(Cmd.insert(_, value))

    def delete: Cmd[Unit] =
      self.flatMap(Cmd.delete)

    def downField(key: String): Cmd[Cursor] =
      self.flatMap(Cmd.downField(_, key))

    def iter: Cmd[Cursor] =
      self.flatMap(Cmd.iter)

    def next: Cmd[Cursor] =
      self.flatMap(Cmd.next)
  }

  implicit def boolean2Val(b: Boolean): Val = if (b) True else False
  implicit def string2Val(s: String): Val = Str(s)
}
