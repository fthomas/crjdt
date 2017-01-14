package eu.timepit.crjdt.circe

import eu.timepit.crjdt.core.Replica
import eu.timepit.crjdt.core.syntax._
import eu.timepit.crjdt.circe.syntax._
import io.circe.Json
import org.scalacheck.Prop._
import org.scalacheck.Properties

object NodeToJsonFigure6Spec extends Properties("NodeToJsonFigure6Spec") {

  val list = v("list")
  val eggs = v("eggs")
  val cmd = (doc := `{}`) `;`
      (let(list) = doc.downField("shopping").iter) `;`
      list.insert("eggs") `;`
      (let(eggs) = list.next) `;`
      eggs.insert("milk") `;`
      list.insert("cheese")

  val document = Replica.empty("").applyCmd(cmd).document

  property("toJson with last-writer-wins conflict resolution") = secure {
    implicit val resolver = RegNodeConflictResolver.LWW
    document.toJson ?= Json.obj(
      "shopping" -> Json.arr(
        List("cheese", "eggs", "milk").map(Json.fromString): _*
      )
    )
  }

  property("toJson with preserve-all-as-array conflict resolution") = secure {
    implicit val resolver = RegNodeConflictResolver.PreserveAllAsArray
    document.toJson ?= Json.obj(
      "shopping" -> Json.arr(
        List("cheese", "eggs", "milk")
          .map(v => Json.arr(Json.fromString(v))): _*
      )
    )
  }
}
