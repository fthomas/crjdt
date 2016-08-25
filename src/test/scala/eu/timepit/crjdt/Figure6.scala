package eu.timepit.crjdt

import eu.timepit.crjdt.Cursor.Key.{DocK, StrK}
import eu.timepit.crjdt.Cursor.Tagged.MapT
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

  property("applyExpr") = secure {
    val cur = Draft.applyExpr(LocalState.empty(""), doc.downField("shopping"))
    cur ?= Cursor(Vector(MapT(DocK)), StrK("shopping"))
  }
}
