package eu.timepit.crjdt.core
package free

import cats.arrow.FunctionK
import cats.data.State
import cats.free.Free
import eu.timepit.crjdt.core.Key.{HeadK, StrK}
import eu.timepit.crjdt.core.Mutation.{AssignM, InsertM}
import eu.timepit.crjdt.core.TypeTag.{ListT, MapT}
import eu.timepit.crjdt.core.free.CmdOp._

sealed trait CmdOp[A] extends Product with Serializable

object CmdOp {
  final case class Assign(cursor: Cursor, value: Val) extends CmdOp[Unit]
  final case class Insert(cursor: Cursor, value: Val) extends CmdOp[Unit]
  final case class Delete(cursor: Cursor) extends CmdOp[Unit]

  case object Doc extends CmdOp[Cursor]
  final case class DownField(cursor: Cursor, key: String) extends CmdOp[Cursor]
  final case class Iter(cursor: Cursor) extends CmdOp[Cursor]
  final case class Next(cursor: Cursor) extends CmdOp[Cursor]

  final case class Keys(cursor: Cursor) extends CmdOp[Set[String]]
  final case class Values(cursor: Cursor) extends CmdOp[List[Val]]

  // interpreter

  type ReplicaM[A] = State[ReplicaState, A]

  new FunctionK[CmdOp, ReplicaM] {
    override def apply[A](fa: CmdOp[A]): ReplicaM[A] =
      fa match {
        // MAKE-ASSIGN
        case Assign(cursor, value) =>
          State.modify(_.makeOp(cursor, AssignM(value)))

        case Insert(cursor, value) =>
          State.modify(_.makeOp(cursor, InsertM(value)))

        case Doc =>
          State.pure(Cursor.doc)

        case DownField(cursor, key) =>
          cursor.finalKey match {
            case HeadK => State.pure(cursor)
            case _ => State.pure(cursor.append(MapT.apply, StrK(key)))
          }

        case Iter(cursor) =>
          State.pure(cursor.append(ListT.apply, HeadK))

        case _ => ???
      }
  }

}

object CmdCompanion {
  def assign(cursor: Cursor, value: Val): Cmd[Unit] =
    Free.liftF(Assign(cursor, value))

  def insert(cursor: Cursor, value: Val): Cmd[Unit] =
    Free.liftF(Insert(cursor, value))

  def delete(cursor: Cursor): Cmd[Unit] =
    Free.liftF(Delete(cursor))

  def doc: Cmd[Cursor] =
    Free.liftF(Doc)

  def downField(cursor: Cursor, key: String): Cmd[Cursor] =
    Free.liftF(DownField(cursor, key))

  def iter(cursor: Cursor): Cmd[Cursor] =
    Free.liftF(Iter(cursor))

  def next(cursor: Cursor): Cmd[Cursor] =
    Free.liftF(Next(cursor))

  def keys(cursor: Cursor): Cmd[Set[String]] =
    Free.liftF(Keys(cursor))

  def values(cursor: Cursor): Cmd[List[Val]] =
    Free.liftF(Values(cursor))
}
