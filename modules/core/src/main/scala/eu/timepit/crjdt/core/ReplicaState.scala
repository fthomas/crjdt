package eu.timepit.crjdt.core

import eu.timepit.crjdt.core.Cmd._
import eu.timepit.crjdt.core.Expr._
import eu.timepit.crjdt.core.Key.{HeadK, StrK}
import eu.timepit.crjdt.core.Operation.Mutation
import eu.timepit.crjdt.core.Operation.Mutation.{AssignM, DeleteM, InsertM}
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
    cmd match {
      case Let(x, expr) => // LET
        val cur = applyExpr(expr)
        copy(variables = variables.updated(x, cur))

      case Assign(expr, value) => // MAKE-ASSIGN
        makeOp(applyExpr(expr), AssignM(value))

      case Insert(expr, value) => // MAKE-INSERT
        makeOp(applyExpr(expr), InsertM(value))

      case Delete(expr) => // MAKE-DELETE
        makeOp(applyExpr(expr), DeleteM)

      case Sequence(cmd1, cmd2) => // EXEC
        applyCmd(cmd1).applyCmd(cmd2)
    }

  @tailrec
  def applyCmds(cmds: List[Cmd]): ReplicaState =
    cmds match {
      case x :: xs => applyCmd(x).applyCmds(xs)
      case Nil => this
    }

  def applyExpr(expr: Expr): Cursor =
    expr match {
      case Doc => // DOC
        Cursor.doc

      case v @ Var(_) => // VAR
        variables.get(v) match {
          case Some(cur) => cur
          // This case violates VAR's precondition x elem dom(A_p).
          case None => Cursor.doc
        }

      case DownField(expr2, key) => // GET
        val cur = applyExpr(expr2)
        cur.finalKey match {
          // This case violates GET's precondition k_n != head.
          // It corresponds to the dubious EXPR `iter[key]` which should
          // be impossible to construct with the EXPR API.
          case HeadK => cur
          case _ => cur.append(MapT.apply, StrK(key))
        }

      case Iter(expr2) => // ITER
        val cur = applyExpr(expr2)
        cur.append(ListT.apply, HeadK)

      case Next(expr2) => // NEXT1
        val cur = applyExpr(expr2)
        context.next(cur)
    }

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
  final def empty(replicaId: ReplicaId): ReplicaState =
    ReplicaState(replicaId = replicaId,
                 opsCounter = 0,
                 context = Context.emptyMap,
                 variables = Map.empty,
                 processedOps = Set.empty,
                 generatedOps = Vector.empty,
                 receivedOps = Vector.empty)
}
