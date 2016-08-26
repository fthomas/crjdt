package eu.timepit.crjdt

import eu.timepit.crjdt.Expr.Next

object Draft {

  type ContextKey = Any

  final case class Context(r: List[(ContextKey, Context)], pres: Set[Id])

  def applyExpr(state: LocalState, expr: Expr): Cursor =
    expr match {
      case Next(e) =>
        val cur = applyExpr(state, e)
        // TODO return cur.next. This needs to examine state for the Ids
        // of the elements in the current list.
        ???
      case _ => ???
    }
}
