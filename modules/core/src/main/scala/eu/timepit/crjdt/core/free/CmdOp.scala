package eu.timepit.crjdt.core
package free

import cats.arrow.FunctionK
import cats.data.State
import eu.timepit.crjdt.core.Key.{HeadK, StrK}
import eu.timepit.crjdt.core.Mutation.{AssignM, DeleteM, InsertM}
import eu.timepit.crjdt.core.TypeTag.{ListT, MapT}

sealed trait CmdOp[A] extends Product with Serializable

object CmdOp {
  final case class Assign(cur: Cursor, value: Val) extends CmdOp[Unit]
  final case class Insert(cur: Cursor, value: Val) extends CmdOp[Unit]
  final case class Delete(cur: Cursor) extends CmdOp[Unit]

  final case class DownField(cur: Cursor, key: String) extends CmdOp[Cursor]
  final case class Iter(cur: Cursor) extends CmdOp[Cursor]
  final case class Next(cur: Cursor) extends CmdOp[Cursor]

  final case class Keys(cur: Cursor) extends CmdOp[Set[String]]
  final case class Values(cur: Cursor) extends CmdOp[List[LeafVal]]

  // interpreters

  type ReplicaState[A] = State[Replica, A]

  val cmdOpToReplicaState: FunctionK[CmdOp, ReplicaState] =
    new FunctionK[CmdOp, ReplicaState] {
      override def apply[A](op: CmdOp[A]): ReplicaState[A] =
        op match {
          // MAKE-ASSIGN
          case Assign(cur, value) =>
            State.modify(_.makeOp(cur, AssignM(value)))

          // MAKE-INSERT
          case Insert(cur, value) =>
            State.modify(_.makeOp(cur, InsertM(value)))

          // MAKE-DELETE
          case Delete(cur) =>
            State.modify(_.makeOp(cur, DeleteM))

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
