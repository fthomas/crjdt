import Cmd.{Let, Sequence}
import Expr.{Doc, Var}

sealed trait Cmd extends Product with Serializable

object Cmd {
  final case class Let(x: Expr.Var, expr: Expr) extends Cmd

  final case class Assign(expr: Expr, v: Val) extends Cmd

  /** Inserts an element into a list. */
  final case class Insert(expr: Expr, v: Val) extends Cmd

  /** Deletes an element from a list or map. */
  final case class Delete(expr: Expr) extends Cmd

  case object Yield extends Cmd

  final case class Sequence(cmd1: Cmd, cmd2: Cmd) extends Cmd
}

// evaluation rules of the command language inspects/modifies this state

object Draft {
  type ReplicaState = Any
  type Cursor = Any

  def applyCmd(state: ReplicaState, cmd: Cmd): ReplicaState =
    cmd match {
      case Sequence(cmd1, cmd2) =>
        applyCmd(applyCmd(state, cmd1), cmd2) // EXEC
      case Let(x, expr) => addVar(state, x, applyExpr(state, expr)) // LET
      case _ => ???
    }

  def addVar(state: ReplicaState, v: Var, cur: Cursor): ReplicaState = ???

  def getVar(state: ReplicaState, v: Var): Option[Cursor] = ??? // VAR

  def applyExpr(state: ReplicaState, expr: Expr): Cursor =
    expr match {
      case Doc => ??? // empty cursor pointing to doc // DOC
      case _ => ???
    }
}
