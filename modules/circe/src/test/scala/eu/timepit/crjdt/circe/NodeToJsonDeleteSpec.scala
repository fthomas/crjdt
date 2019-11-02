package eu.timepit.crjdt.circe

import org.scalacheck.Properties
import org.scalacheck.Prop._
import eu.timepit.crjdt.circe.RegNodeConflictResolver.LWW
import eu.timepit.crjdt.circe.syntax._
import eu.timepit.crjdt.core.Replica
import eu.timepit.crjdt.core.syntax._
import io.circe.Json

object NodeToJsonDeleteSpec extends Properties("NodeToJsonDeleteSpec") {
  // fix bug #19
  property("delete list items") = secure {
    val list = doc.downField("list")
    val p = Replica
      .empty("p")
      .applyCmd(list := `[]`)
      .applyCmd(list.iter.insert("1"))
      .applyCmd(list.iter.next.insert("2"))
      .applyCmd(list.iter.next.delete)
    p.document.toJson ?= Json.obj(
      "list" -> Json.arr(Json.fromString("2"))
    )
  }
}
