package eu.timepit.crjdt.core

import cats.instances.list._
import eu.timepit.crjdt.core.Cmd._
import eu.timepit.crjdt.core.Expr._
import eu.timepit.crjdt.core.Key.{HeadK, StrK}
import eu.timepit.crjdt.core.Mutation.{AssignM, DeleteM, InsertM}
import eu.timepit.crjdt.core.TypeTag.{ListT, MapT}
import eu.timepit.crjdt.core.util.applyAllLeft

import scala.annotation.tailrec

final case class Replica(replicaId: ReplicaId,
                         opsCounter: BigInt,
                         document: Node,
                         variables: Map[Var, Cursor],
                         processedOps: Set[Id],
                         generatedOps: Vector[Operation],
                         receivedOps: Vector[Operation]) {

  def applyCmd(cmd: Cmd): Replica =
    Replica.applyCmds(this, List(cmd))

  def applyCmds(cmds: List[Cmd]): Replica =
    Replica.applyCmds(this, cmds)

  // APPLY-LOCAL
  def applyLocal(op: Operation): Replica =
    copy(document = document.applyOp(op),
         processedOps = processedOps + op.id,
         generatedOps = generatedOps :+ op)

  // APPLY-REMOTE, YIELD
  @tailrec
  def applyRemote: Replica =
    findApplicableRemoteOp match {
      case None => this
      case Some(op) =>
        copy(opsCounter = opsCounter max op.id.c,
             document = document.applyOp(op),
             processedOps = processedOps + op.id).applyRemote
    }

  // RECV, YIELD
  def applyRemoteOps(ops: Vector[Operation]): Replica =
    copy(receivedOps = ops ++ receivedOps).applyRemote

  def currentId: Id =
    Id(opsCounter, replicaId)

  def evalExpr(expr: Expr): Cursor = {
    @tailrec
    def go(expr: Expr, fs: List[Cursor => Cursor]): Cursor =
      expr match {
        // DOC
        case Doc => applyAllLeft(fs, Cursor.doc)

        // VAR
        case v @ Var(_) =>
          variables.get(v) match {
            case Some(cur) => applyAllLeft(fs, cur)
            // This case violates VAR's precondition x elem dom(A_p).
            case None => applyAllLeft(fs, Cursor.doc)
          }

        // GET
        case DownField(expr2, key) =>
          val f = (c: Cursor) =>
            c.finalKey match {
              // This case violates GET's precondition k_n != head.
              // It corresponds to the dubious EXPR `iter[key]` which should
              // be impossible to construct with the EXPR API.
              case HeadK => c
              case _ => c.append(MapT.apply, StrK(key))
          }
          go(expr2, f :: fs)

        // ITER
        case Iter(expr2) =>
          val f = (c: Cursor) => c.append(ListT.apply, HeadK)
          go(expr2, f :: fs)

        // NEXT1
        case Next(expr2) =>
          val f = document.next _
          go(expr2, f :: fs)
      }
    go(expr, List.empty)
  }

  /** Finds an `[[Operation]]` in `[[receivedOps]]` that has not
    * already been processed and whose causal dependencies are
    * satisfied.
    */
  def findApplicableRemoteOp: Option[Operation] =
    receivedOps.find { op =>
      !processedOps(op.id) && op.deps.subsetOf(processedOps)
    }

  def incrementCounter: Replica =
    copy(opsCounter = opsCounter + 1)

  // KEYS1
  def keys(expr: Expr): Set[String] =
    document.keys(evalExpr(expr))

  // MAKE-OP
  def makeOp(cur: Cursor, mut: Mutation): Replica = {
    val newReplica = incrementCounter
    val op = Operation(newReplica.currentId, newReplica.processedOps, cur, mut)
    newReplica.applyLocal(op)
  }

  // VAL1
  def values(expr: Expr): List[LeafVal] =
    document.values(evalExpr(expr))
}

object Replica {
  @tailrec
  final def applyCmds(replica: Replica, cmds: List[Cmd]): Replica =
    cmds match {
      case cmd :: rest =>
        cmd match {
          // LET
          case Let(x, expr) =>
            val cur = replica.evalExpr(expr)
            val newReplica =
              replica.copy(variables = replica.variables.updated(x, cur))
            applyCmds(newReplica, rest)

          // MAKE-ASSIGN
          case Assign(expr, value) =>
            val newReplica =
              replica.makeOp(replica.evalExpr(expr), AssignM(value))
            applyCmds(newReplica, rest)

          // MAKE-INSERT
          case Insert(expr, value) =>
            val newReplica =
              replica.makeOp(replica.evalExpr(expr), InsertM(value))
            applyCmds(newReplica, rest)

          // MAKE-DELETE
          case Delete(expr) =>
            val newReplica = replica.makeOp(replica.evalExpr(expr), DeleteM)
            applyCmds(newReplica, rest)

          // EXEC
          case Sequence(cmd1, cmd2) =>
            applyCmds(replica, cmd1 :: cmd2 :: rest)
        }
      case Nil => replica
    }

  final def empty(replicaId: ReplicaId): Replica =
    Replica(replicaId = replicaId,
            opsCounter = 0,
            document = Node.emptyMap,
            variables = Map.empty,
            processedOps = Set.empty,
            generatedOps = Vector.empty,
            receivedOps = Vector.empty)
}
