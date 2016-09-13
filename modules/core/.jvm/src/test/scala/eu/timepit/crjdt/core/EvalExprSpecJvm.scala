package eu.timepit.crjdt.core

import eu.timepit.crjdt.core.syntax._
import org.scalacheck.Prop._
import org.scalacheck.Properties

object EvalExprSpecJvm extends Properties("ReplicaState.evalExpr") {
  val state = ReplicaState.empty("p")

  property("stack safety") = secure {
    val count = 100000
    val expr = Iterator.iterate(doc)(_.downField("k")).drop(count).next()
    val cur = state.evalExpr(expr)
    cur.keys.size ?= count
  }
}
