package eu.timepit.crjdt

import eu.timepit.crjdt.Cursor.Key.{DocK, HeadK, StrK}
import eu.timepit.crjdt.Cursor.Tagged.{ListT, MapT}
import eu.timepit.crjdt.Expr.{Doc, DownField, Iter, Next}

object Draft {

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
      case Next(e) =>
        val cur = applyExpr(state, e)
        // TODO return cur.next. This needs to examine state for the Ids
        // of the elements in the current list.
        ???
      case _ => ???
    }
}
