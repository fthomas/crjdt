package eu.timepit.crjdt.core
package free

import cats.free.Free
import eu.timepit.crjdt.core.free.CmdOp._

object CmdCompanion {

  // constructors

  def assign(cur: Cursor, value: Val): Cmd[Unit] =
    Free.liftF(Assign(cur, value))

  def insert(cur: Cursor, value: Val): Cmd[Unit] =
    Free.liftF(Insert(cur, value))

  def delete(cur: Cursor): Cmd[Unit] =
    Free.liftF(Delete(cur))

  def downField(cur: Cursor, key: String): Cmd[Cursor] =
    Free.liftF(DownField(cur, key))

  def iter(cur: Cursor): Cmd[Cursor] =
    Free.liftF(Iter(cur))

  def next(cur: Cursor): Cmd[Cursor] =
    Free.liftF(Next(cur))

  def keys(cur: Cursor): Cmd[Set[String]] =
    Free.liftF(Keys(cur))

  def values(cur: Cursor): Cmd[List[LeafVal]] =
    Free.liftF(Values(cur))

  // derived operations

  def insertAll(cur: Cursor, values: List[Val]): Cmd[Unit] =
    values match {
      case Nil => Free.pure(())
      case head :: tail =>
        for {
          _ <- insert(cur, head)
          nextCur <- next(cur)
          _ <- insertAll(nextCur, tail)
        } yield ()
    }
}
