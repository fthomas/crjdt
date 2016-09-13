package eu.timepit.crjdt.core

import eu.timepit.crjdt.core.Key.{DocK, HeadK, StrK}
import eu.timepit.crjdt.core.TypeTag.{ListT, MapT}
import eu.timepit.crjdt.core.syntax._
import org.scalacheck.Prop._
import org.scalacheck.Properties

object EvalExprSpec extends Properties("ReplicaState.evalExpr") {
  val state = ReplicaState.empty("p")

  property("doc") = secure {
    state.evalExpr(doc) ?= Cursor.withFinalKey(DocK)
  }

  property("""doc["key"]""") = secure {
    state.evalExpr(doc.downField("key")) ?=
      Cursor(Vector(MapT(DocK)), StrK("key"))
  }

  property("""doc["key"].iter""") = secure {
    state.evalExpr(doc.downField("key").iter) ?=
      Cursor(Vector(MapT(DocK), ListT(StrK("key"))), HeadK)
  }

  property("doc.iter") = secure {
    state.evalExpr(doc.iter) ?= Cursor(Vector(ListT(DocK)), HeadK)
  }

  property("doc.iter.next") = secure {
    state.evalExpr(doc.iter.next) ?= Cursor(Vector(ListT(DocK)), HeadK)
  }
}
