package eu.timepit.crjdt

import eu.timepit.crjdt.Cmd.{Let, Sequence}
import eu.timepit.crjdt.Cursor.Key.{DocK, HeadK, StrK}
import eu.timepit.crjdt.Cursor.Tagged.{ListT, MapT}
import eu.timepit.crjdt.Expr.{Doc, DownField, Iter}

sealed trait Cmd extends Product with Serializable

object Cmd {

  /** Sets the value of a local variable. */
  final case class Let(x: Expr.Var, expr: Expr) extends Cmd

  final case class Assign(expr: Expr, v: Val) extends Cmd

  /** Inserts an element into a list. */
  final case class Insert(expr: Expr, v: Val) extends Cmd

  /** Deletes an element from a list or map. */
  final case class Delete(expr: Expr) extends Cmd

  /** Performs network communication. */
  case object Yield extends Cmd

  final case class Sequence(cmd1: Cmd, cmd2: Cmd) extends Cmd
}

// evaluation rules of the command language inspects/modifies this state

object Draft {

  def applyCmd(state: LocalState, cmd: Cmd): LocalState =
    cmd match {
      case Sequence(cmd1, cmd2) =>
        applyCmd(applyCmd(state, cmd1), cmd2) // EXEC
      case Let(x, expr) => state.addVar(x, applyExpr(state, expr)) // LET
      case _ => ???
    }

  def applyExpr(state: LocalState, expr: Expr): Cursor =
    expr match {
      case Doc => Cursor(Vector.empty, DocK) // DOC
      case DownField(e, key) =>
        applyExpr(state, e) match {
          case Cursor(ks, kn) if kn != HeadK =>
            Cursor(ks :+ MapT(kn), StrK(key)) // GET
          case _ => ???
        }
      case Iter(e) =>
        val c = applyExpr(state, e)
        Cursor(c.keys :+ ListT(c.finalKey), HeadK) // ITER
      case _ => ???
    }
}
