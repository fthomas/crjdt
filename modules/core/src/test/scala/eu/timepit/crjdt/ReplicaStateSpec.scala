package eu.timepit.crjdt

import eu.timepit.crjdt.Key.{DocK, HeadK, StrK}
import eu.timepit.crjdt.Tag.{ListT, MapT}
import eu.timepit.crjdt.syntax._
import org.scalacheck.Prop._
import org.scalacheck.Properties

object ReplicaStateSpec extends Properties("ReplicaState") {
  val emptyState = ReplicaState.empty("")

  property("""applyExpr doc["shopping"]""") = secure {
    val shopping = "shopping"
    val cur = emptyState.applyExpr(doc.downField(shopping))
    cur ?= Cursor(Vector(MapT(DocK)), StrK(shopping))
  }

  property("""applyExpr doc["shopping"].iter""") = secure {
    val shopping = "shopping"
    val cur = emptyState.applyExpr(doc.downField(shopping).iter)
    cur ?= Cursor(Vector(MapT(DocK), ListT(StrK(shopping))), HeadK)
  }

  property("""applyExpr doc["key1"]["key2"]""") = secure {
    val (key1, key2) = ("key1", "key2")
    val cur = emptyState.applyExpr(doc.downField(key1).downField(key2))
    cur ?= Cursor(Vector(MapT(DocK), MapT(StrK(key1))), StrK(key2))
  }

  property("""applyCmd let(x) := doc["key"]""") = secure {
    val x = v("x")
    val key = "key"
    val cmd = let(x) = doc.downField(key)
    val state = emptyState.applyCmd(cmd)
    state.applyExpr(x) ?= Cursor(Vector(MapT(DocK)), StrK(key))
  }

  property("applyCmd doc := {}") = secure {
    val cmd = (doc := `{}`) `;`
        (doc.downField("key1") := Val.Str("hallo")) `;`
        doc.downField("key1").delete
    println(emptyState.applyCmd(cmd).context) //?= emptyState.ctx
    true
  }

  property("delete map") = secure {
    val cmd = (doc := `{}`) `;`
        (doc.downField("key1") := `{}`) `;`
        (doc.downField("key1").downField("key2") := Val.Str("hallo")) `;`
        doc.downField("key1").delete
    println(emptyState.applyCmd(cmd).context) //?= emptyState.ctx
    true
  }
}
