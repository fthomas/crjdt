package eu.timepit.crjdt.circe

import eu.timepit.crjdt.core.Replica
import eu.timepit.crjdt.core.syntax._
import eu.timepit.crjdt.circe.testUtil.{converged, diverged, merge}
import eu.timepit.crjdt.circe.syntax._
import io.circe.Json
import org.scalacheck.Prop._
import org.scalacheck.Properties

object NodeToJsonFigure1Spec extends Properties("NodeToJsonFigure1Spec") {

  val p0 = Replica.empty("p").applyCmd(doc.downField("key") := "A")
  val q0 = merge(Replica.empty("q"), p0)

  property("initial state") = secure {
    converged(p0, q0)
  }

  val p1 = p0.applyCmd(doc.downField("key") := "B")
  val q1 = q0.applyCmd(doc.downField("key") := "C")

  property("divergence") = secure {
    diverged(p1, q1)
  }

  val p2 = merge(p1, q1)
  val q2 = merge(q1, p1)

  property("convergence") = secure {
    converged(p2, q2)
  }

  property("toJson with last-writer-wins conflict resolution") = secure {
    implicit val resolver = RegNodeConflictResolver.LWW
    p2.document.toJson ?= Json.obj(
      "key" -> Json.fromString("C")
    )
  }

  property("toJson with preserve-all-as-array conflict resolution") = secure {
    implicit val resolver = RegNodeConflictResolver.PreserveAllAsArray
    p2.document.toJson ?= Json.obj(
      "key" -> Json.arr(List("B", "C").map(Json.fromString): _*)
    )
  }
}
