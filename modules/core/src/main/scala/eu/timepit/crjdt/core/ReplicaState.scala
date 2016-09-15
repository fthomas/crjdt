package eu.timepit.crjdt.core

import cats.Eval
import eu.timepit.crjdt.core.Cmd._
import eu.timepit.crjdt.core.Expr._
import eu.timepit.crjdt.core.Key.{HeadK, StrK}
import eu.timepit.crjdt.core.Mutation.{AssignM, DeleteM, InsertM}
import eu.timepit.crjdt.core.TypeTag.{ListT, MapT}

import scala.annotation.tailrec

final case class ReplicaState(replicaId: ReplicaId,
                              opsCounter: BigInt,
                              context: Context,
                              variables: Map[Var, Cursor],
                              processedOps: Set[Id],
                              generatedOps: Vector[Operation],
                              receivedOps: Vector[Operation]) {

  def applyCmd(cmd: Cmd): ReplicaState =
    ReplicaState.applyCmds(this, List(cmd))

  def applyCmds(cmds: List[Cmd]): ReplicaState =
    ReplicaState.applyCmds(this, cmds)

  // APPLY-LOCAL
  def applyLocal(op: Operation): ReplicaState =
    copy(context = context.applyOp(op),
         processedOps = processedOps + op.id,
         generatedOps = generatedOps :+ op)

  // APPLY-REMOTE, YIELD
  @tailrec
  def applyRemote: ReplicaState =
    findApplicableRemoteOp match {
      case None => this
      case Some(op) =>
        copy(opsCounter = opsCounter max op.id.c,
             context = context.applyOp(op),
             processedOps = processedOps + op.id).applyRemote
    }

  // RECV, YIELD
  def applyRemoteOps(ops: Vector[Operation]): ReplicaState =
    copy(receivedOps = ops ++ receivedOps).applyRemote

  def currentId: Id =
    Id(opsCounter, replicaId)

  def evalExpr(expr: Expr): Cursor = {
    def go(expr: Expr): Eval[Cursor] =
      expr match {
        case Doc => // DOC
          Eval.now(Cursor.doc)

        case v @ Var(_) => // VAR
          variables.get(v) match {
            case Some(cur) => Eval.now(cur)
            // This case violates VAR's precondition x elem dom(A_p).
            case None => Eval.now(Cursor.doc)
          }

        case DownField(expr2, key) => // GET
          val cur = Eval.defer(go(expr2))
          cur.map { c =>
            c.finalKey match {
              // This case violates GET's precondition k_n != head.
              // It corresponds to the dubious EXPR `iter[key]` which should
              // be impossible to construct with the EXPR API.
              case HeadK => c
              case _ => c.append(MapT.apply, StrK(key))
            }
          }

        case Iter(expr2) => // ITER
          val cur = Eval.defer(go(expr2))
          cur.map(_.append(ListT.apply, HeadK))

        case Next(expr2) => // NEXT1
          val cur = Eval.defer(go(expr2))
          cur.map(context.next)
      }
    go(expr).value
  }

  /** Finds an `[[Operation]]` in `[[receivedOps]]` that has not
    * already been processed and whose causal dependencies are
    * satisfied.
    */
  def findApplicableRemoteOp: Option[Operation] =
    receivedOps.find { op =>
      !processedOps(op.id) && op.deps.subsetOf(processedOps)
    }

  def incrementCounter: ReplicaState =
    copy(opsCounter = opsCounter + 1)

  // MAKE-OP
  def makeOp(cur: Cursor, mut: Mutation): ReplicaState = {
    val newState = incrementCounter
    val op = Operation(newState.currentId, newState.processedOps, cur, mut)
    newState.applyLocal(op)
  }
}

object ReplicaState {
  @tailrec
  final def applyCmds(state: ReplicaState, cmds: List[Cmd]): ReplicaState =
    cmds match {
      case cmd :: rest =>
        cmd match {
          case Let(x, expr) => // LET
            val cur = state.evalExpr(expr)
            val next = state.copy(variables = state.variables.updated(x, cur))
            applyCmds(next, rest)

          case Assign(expr, value) => // MAKE-ASSIGN
            val next = state.makeOp(state.evalExpr(expr), AssignM(value))
            applyCmds(next, rest)

          case Insert(expr, value) => // MAKE-INSERT
            val next = state.makeOp(state.evalExpr(expr), InsertM(value))
            applyCmds(next, rest)

          case Delete(expr) => // MAKE-DELETE
            val next = state.makeOp(state.evalExpr(expr), DeleteM)
            applyCmds(next, rest)

          case Sequence(cmd1, cmd2) => // EXEC
            applyCmds(state, cmd1 :: cmd2 :: rest)
        }
      case Nil => state
    }

  final def empty(replicaId: ReplicaId): ReplicaState =
    ReplicaState(replicaId = replicaId,
                 opsCounter = 0,
                 context = Context.emptyMap,
                 variables = Map.empty,
                 processedOps = Set.empty,
                 generatedOps = Vector.empty,
                 receivedOps = Vector.empty)
}
