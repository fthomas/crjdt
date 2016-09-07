package eu.timepit.crjdt.core

import eu.timepit.crjdt.core.Cmd.{Assign, Delete, Insert}
import eu.timepit.crjdt.core.Expr.{Doc, DownField, Iter, Var}
import eu.timepit.crjdt.core.Val.EmptyList
import eu.timepit.crjdt.core.syntax._
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
