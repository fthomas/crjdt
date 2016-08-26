package eu.timepit.crjdt

import eu.timepit.crjdt.Cmd._
import eu.timepit.crjdt.Cursor.Key.{DocK, HeadK, StrK}
import eu.timepit.crjdt.Cursor.Tagged.{ListT, MapT}
import eu.timepit.crjdt.Expr.{Doc, DownField, Iter, Var}
import eu.timepit.crjdt.Operation.Mutation
import eu.timepit.crjdt.Operation.Mutation.{AssignM, DeleteM, InsertM}

/*
TODO:
- NEXT1,2,3,4
- KEYS1,2,3
- VAL1,2,3

- APPLY-LOCAL
- APPLY-REMOTE
- SEND
- RECV
- YIELD
 */

final case class LocalState(ctx: Context,
                            replicaId: ReplicaId,
                            opsCounter: BigInt,
                            variables: Map[Var, Cursor],
                            processedOps: Set[Id],
                            generatedOps: Vector[Operation]) {

  def addVar(x: Var, cur: Cursor): LocalState =
    copy(variables = variables.updated(x, cur))

  def applyCmd(cmd: Cmd): LocalState =
    cmd match {
      case Let(x, expr) => // LET
        addVar(x, applyExpr(expr))

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
      case Doc => // DOC
        Cursor.withFinalKey(DocK)

      case DownField(expr2, key) => // GET
        val cur = applyExpr(expr2)
        cur.finalKey match {
          case HeadK => cur
          case _ => cur.push(MapT.apply, StrK(key))
        }

      case Iter(expr2) => // ITER
        val cur = applyExpr(expr2)
        cur.push(ListT.apply, HeadK)

      case _ =>
        ???
    }

  // APPLY-LOCAL
  def applyLocal(op: Operation): LocalState =
    copy(ctx = ctx.applyOp(op),
         processedOps = processedOps + op.id,
         generatedOps = generatedOps :+ op)

  def currentId: Id =
    Id(opsCounter, replicaId)

  // VAR
  def getVar(x: Var): Option[Cursor] =
    variables.get(x)

  def increaseCounterTo(c: BigInt): LocalState =
    if (opsCounter < c) copy(opsCounter = c) else this

  def incrementCounter: LocalState =
    copy(opsCounter = opsCounter + 1)

  // MAKE-OP
  def makeOp(cur: Cursor, mut: Mutation): LocalState = {
    val newState = incrementCounter
    val op = Operation(newState.currentId, newState.processedOps, cur, mut)
    newState.applyLocal(op)
  }
}

object LocalState {
  def empty(replicaId: ReplicaId): LocalState =
    LocalState(ctx = Context.empty,
               replicaId = replicaId,
               opsCounter = 0,
               variables = Map.empty,
               processedOps = Set.empty,
               generatedOps = Vector.empty)
}
