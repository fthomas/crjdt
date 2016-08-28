package eu.timepit.crjdt

import eu.timepit.crjdt.Cursor.Tagged.{ListT, MapT}
import eu.timepit.crjdt.Key.{DocK, HeadK, StrK}
import eu.timepit.crjdt.syntax._
import org.scalacheck.Prop._
import org.scalacheck.Properties

class LocalStateSpec extends Properties("LocalStateSpec") {
  property("""applyExpr doc["shopping"]""") = secure {
    val shopping = "shopping"
    val cur = LocalState.empty("").applyExpr(doc.downField(shopping))
    cur ?= Cursor(Vector(MapT(DocK)), StrK(shopping))
  }

  property("""applyExpr doc["shopping"].iter""") = secure {
    val shopping = "shopping"
    val cur = LocalState.empty("").applyExpr(doc.downField(shopping).iter)
    cur ?= Cursor(Vector(MapT(DocK), ListT(StrK(shopping))), HeadK)
  }

  property("""applyCmd let(x) := doc["key"]""") = secure {
    val x = v("x")
    val key = "key"
    val cmd = let(x) = doc.downField(key)
    val state = LocalState.empty("").applyCmd(cmd)
    state.applyExpr(x) ?= Cursor(Vector(MapT(DocK)), StrK(key))
  }
}
