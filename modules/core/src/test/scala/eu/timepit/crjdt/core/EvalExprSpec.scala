package eu.timepit.crjdt.core

import eu.timepit.crjdt.core.Key.{DocK, HeadK, IdK, StrK}
import eu.timepit.crjdt.core.TypeTag.{ListT, MapT}
import eu.timepit.crjdt.core.syntax._
import org.scalacheck.Prop._
import org.scalacheck.Properties

object EvalExprSpec extends Properties("Replica.evalExpr") {
  val p0 = Replica.empty("p")

  property("doc") = secure {
    p0.evalExpr(doc) ?= Cursor.withFinalKey(DocK)
  }

  property("""doc["key"]""") = secure {
    p0.evalExpr(doc.downField("key")) ?=
      Cursor(Vector(MapT(DocK)), StrK("key"))
  }

  property("""doc["key"].iter""") = secure {
    p0.evalExpr(doc.downField("key").iter) ?=
      Cursor(Vector(MapT(DocK), ListT(StrK("key"))), HeadK)
  }

  property("doc.iter") = secure {
    p0.evalExpr(doc.iter) ?= Cursor(Vector(ListT(DocK)), HeadK)
  }

  property("doc.iter.next") = secure {
    p0.evalExpr(doc.iter.next) ?= Cursor(Vector(ListT(DocK)), HeadK)
  }

  property("list.iter.next") = secure {
    val list = doc.downField("list")
    val cmd = (list := `[]`) `;`
      list.iter.insert("item1") `;`
      list.iter.insert("item2") `;`
      list.iter.insert("item3")

    val p1 = p0.applyCmd(cmd)
    val e1 = list.iter.next
    val e2 = list.iter.next.next
    val e3 = list.iter.next.next.next
    val cur = p1.evalExpr(list.iter)

    (p1.evalExpr(e1) ?= cur.copy(finalKey = IdK(Id(4, "p")))) &&
    (p1.evalExpr(e2) ?= cur.copy(finalKey = IdK(Id(3, "p")))) &&
    (p1.evalExpr(e3) ?= cur.copy(finalKey = IdK(Id(2, "p"))))
  }
}
