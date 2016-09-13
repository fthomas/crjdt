package eu.timepit.crjdt.core

import eu.timepit.crjdt.core.Key.{DocK, HeadK, StrK}
import eu.timepit.crjdt.core.TypeTag.{ListT, MapT}
import eu.timepit.crjdt.core.syntax._
import org.scalacheck.Prop._
import org.scalacheck.Properties

object ApplyExprSpec extends Properties("ReplicaState.applyExpr") {
  val state = ReplicaState.empty("p")

  property("doc") = secure {
    state.applyExpr(doc) ?= Cursor.withFinalKey(DocK)
  }

  property("""doc["key"]""") = secure {
    state.applyExpr(doc.downField("key")) ?=
      Cursor(Vector(MapT(DocK)), StrK("key"))
  }

  property("""doc["key"].iter""") = secure {
    state.applyExpr(doc.downField("key").iter) ?=
      Cursor(Vector(MapT(DocK), ListT(StrK("key"))), HeadK)
  }

  property("doc.iter") = secure {
    state.applyExpr(doc.iter) ?= Cursor(Vector(ListT(DocK)), HeadK)
  }

  property("doc.iter.next") = secure {
    state.applyExpr(doc.iter.next) ?= Cursor(Vector(ListT(DocK)), HeadK)
  }

  property("stack safety") = secure {
    val expr = Iterator.iterate(doc)(_.downField("k")).drop(20000).next()
    // println(expr)
    // val cur = ReplicaState.empty("").applyExpr(expr)
    // println(cur)
    true
  }
}
