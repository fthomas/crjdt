package eu.timepit.crjdt

import eu.timepit.crjdt.Cursor.Key.{DocK, HeadK, StrK}
import eu.timepit.crjdt.Cursor.Tagged.{ListT, MapT}
import eu.timepit.crjdt.syntax._
import org.scalacheck.Prop._
import org.scalacheck.Properties

class Figure6 extends Properties("Figure6") {
  property("Doc") = secure {
    val list = v("list")
    val eggs = v("eggs")
    (doc := `{}`) `;`
      (let(list) = doc.downField("shopping").iter) `;`
      list.insert("eggs") `;`
      (let(eggs) = list.next) `;`
      eggs.insert("milk") `;`
      list.insert("cheese")
    true
  }

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
}
