package eu.timepit.crjdt.core
package free

import cats.free.Free
import eu.timepit.crjdt.core.free.CmdOp._

sealed trait CmdOp[T] extends Product with Serializable

object CmdOp {
  final case class Assign(expr: Expr, value: Val) extends CmdOp[Unit]
  final case class Insert(expr: Expr, value: Val) extends CmdOp[Unit]
  final case class Delete(expr: Expr) extends CmdOp[Unit]

  final case class Keys(expr: Expr) extends CmdOp[Set[String]]
  final case class Values(expr: Expr) extends CmdOp[List[Val]]
}

object CmdCompanion {
  def assign(expr: Expr, value: Val): Cmd[Unit] =
    Free.liftF(Assign(expr, value))

  def insert(expr: Expr, value: Val): Cmd[Unit] =
    Free.liftF(Insert(expr, value))

  def delete(expr: Expr): Cmd[Unit] =
    Free.liftF(Delete(expr))

  def keys(expr: Expr): Cmd[Set[String]] =
    Free.liftF(Keys(expr))

  def values(expr: Expr): Cmd[List[Val]] =
    Free.liftF(Values(expr))
}
