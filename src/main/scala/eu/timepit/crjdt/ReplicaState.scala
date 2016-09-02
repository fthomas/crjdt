package eu.timepit.crjdt

import eu.timepit.crjdt.Cmd._
import eu.timepit.crjdt.Expr._
import eu.timepit.crjdt.Key.{HeadK, StrK}
import eu.timepit.crjdt.Operation.Mutation
import eu.timepit.crjdt.Operation.Mutation.{AssignM, DeleteM, InsertM}
import eu.timepit.crjdt.Tag.{ListT, MapT}

final case class ReplicaState(replicaId: ReplicaId,
                              opsCounter: BigInt,
                              context: Context,
                              variables: Map[Var, Cursor],
                              processedOps: Set[Id],
                              generatedOps: Vector[Operation]) {

  def applyCmd(cmd: Cmd): ReplicaState =
    cmd match {
      case Let(x, expr) => // LET
        val cur = applyExpr(expr)
        copy(variables = variables.updated(x, cur))

      case Assign(expr, v) => // MAKE-ASSIGN
        makeOp(applyExpr(expr), AssignM(v))

      case Insert(expr, v) => // MAKE-INSERT
        makeOp(applyExpr(expr), InsertM(v))

      case Delete(expr) => // MAKE-DELETE
        makeOp(applyExpr(expr), DeleteM)

      case Yield =>
        ???

      case Sequence(cmd1, cmd2) => // EXEC
        applyCmd(cmd1).applyCmd(cmd2)
    }

  def applyExpr(expr: Expr): Cursor =
    expr match {
      // DOC
      case Doc => Cursor.doc

      // VAR
      case v @ Var(_) =>
        variables.get(v) match {
          case Some(cur) => cur
          // This case violates VAR's precondition x elem dom(A_p).
          case None => Cursor.doc
        }

      // GET
      case DownField(expr2, key) =>
        val cur = applyExpr(expr2)
        cur.finalKey match {
          // This case violates GET's precondition k_n != head.
          // It corresponds to the dubious EXPR `iter[key]` which should
          // be impossible to construct with the EXPR API.
          case HeadK => cur
          case _ => cur.push(MapT.apply, StrK(key))
        }

      // ITER
      case Iter(expr2) =>
        val cur = applyExpr(expr2)
        cur.push(ListT.apply, HeadK)

      case Next(expr2) =>
        val cur = applyExpr(expr2)
        ???
    }

  // APPLY-LOCAL
  def applyLocal(op: Operation): ReplicaState =
    copy(context = context.applyOp(op),
         processedOps = processedOps + op.id,
         generatedOps = generatedOps :+ op)

  def currentId: Id =
    Id(opsCounter, replicaId)

  def increaseCounterTo(c: BigInt): ReplicaState =
    if (opsCounter < c) copy(opsCounter = c) else this

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
  def empty(replicaId: ReplicaId): ReplicaState =
    ReplicaState(replicaId = replicaId,
                 opsCounter = 0,
                 context = Context.emptyMap,
                 variables = Map.empty,
                 processedOps = Set.empty,
                 generatedOps = Vector.empty)
}
