package eu.timepit.crjdt.core
package free

import cats.arrow.FunctionK
import cats.data.State
import cats.free.Free
import eu.timepit.crjdt.core.Key.{HeadK, StrK}
import eu.timepit.crjdt.core.Mutation.{AssignM, DeleteM, InsertM}
import eu.timepit.crjdt.core.TypeTag.{ListT, MapT}
import eu.timepit.crjdt.core.free.CmdOp._

sealed trait CmdOp[A] extends Product with Serializable

object CmdOp {
  final case class Assign(cur: Cursor, value: Val) extends CmdOp[Unit]
  final case class Insert(cur: Cursor, value: Val) extends CmdOp[Unit]
  final case class Delete(cur: Cursor) extends CmdOp[Unit]

  case object Doc extends CmdOp[Cursor]
  final case class DownField(cur: Cursor, key: String) extends CmdOp[Cursor]
  final case class Iter(cur: Cursor) extends CmdOp[Cursor]
  final case class Next(cur: Cursor) extends CmdOp[Cursor]

  final case class Keys(cur: Cursor) extends CmdOp[Set[String]]
  final case class Values(cur: Cursor) extends CmdOp[List[Val]]

  // interpreters

  type ReplicaState[A] = State[Replica, A]

  val interpreter = new FunctionK[CmdOp, ReplicaState] {
    override def apply[A](fa: CmdOp[A]): ReplicaState[A] =
      fa match {
        // MAKE-ASSIGN
        case Assign(cur, value) =>
          State.modify(_.makeOp(cur, AssignM(value)))

        // MAKE-INSERT
        case Insert(cur, value) =>
          State.modify(_.makeOp(cur, InsertM(value)))

        // MAKE-DELETE
        case Delete(cur) =>
          State.modify(_.makeOp(cur, DeleteM))

        // DOC
        case Doc =>
          State.pure(Cursor.doc)

        // GET
        case DownField(cur, key) =>
          cur.finalKey match {
            case HeadK => State.pure(cur)
            case _ => State.pure(cur.append(MapT.apply, StrK(key)))
          }

        // ITER
        case Iter(cur) =>
          State.pure(cur.append(ListT.apply, HeadK))

        // NEXT1
        case Next(cur) =>
          State.inspect(_.document.next(cur))

        // KEYS1
        case Keys(cur) =>
          State.inspect(_.document.keys(cur))

        // VAL1
        case Values(cur) =>
          State.inspect(_.document.values(cur))
      }
  }

}

object CmdCompanion {
  def assign(cur: Cursor, value: Val): Cmd[Unit] =
    Free.liftF(Assign(cur, value))

  def insert(cur: Cursor, value: Val): Cmd[Unit] =
    Free.liftF(Insert(cur, value))

  def delete(cur: Cursor): Cmd[Unit] =
    Free.liftF(Delete(cur))

  def doc: Cmd[Cursor] =
    Free.liftF(Doc)

  def downField(cur: Cursor, key: String): Cmd[Cursor] =
    Free.liftF(DownField(cur, key))

  def iter(cur: Cursor): Cmd[Cursor] =
    Free.liftF(Iter(cur))

  def next(cur: Cursor): Cmd[Cursor] =
    Free.liftF(Next(cur))

  def keys(cur: Cursor): Cmd[Set[String]] =
    Free.liftF(Keys(cur))

  def values(cur: Cursor): Cmd[List[Val]] =
    Free.liftF(Values(cur))
}
