package eu.timepit.crjdt

import eu.timepit.crjdt.Cmd.{Assign, Delete, Insert}
import eu.timepit.crjdt.Expr.{Doc, DownField, Iter, Var}
import eu.timepit.crjdt.Val.EmptyList
import eu.timepit.crjdt.syntax._
import org.scalacheck.Prop._
import org.scalacheck.Properties

object SyntaxSpec extends Properties("syntax") {
  property("assign") = secure {
    v("list") := `[]` ?= Assign(Var("list"), EmptyList)
  }

  property("insert") = secure {
    doc.downField("key").iter.insert(Val.Null) ?=
      Insert(Iter(DownField(Doc, "key")), Val.Null)
  }

  property("delete") = secure {
    doc.delete ?= Delete(Doc)
  }
}
