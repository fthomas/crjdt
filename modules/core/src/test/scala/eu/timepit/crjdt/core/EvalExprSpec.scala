package eu.timepit.crjdt.core

import eu.timepit.crjdt.core.Key.{DocK, HeadK, StrK}
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
}
