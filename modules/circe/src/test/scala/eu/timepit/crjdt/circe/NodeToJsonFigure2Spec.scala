package eu.timepit.crjdt.circe

import eu.timepit.crjdt.core.Replica
import eu.timepit.crjdt.core.syntax._
import eu.timepit.crjdt.circe.syntax._
import eu.timepit.crjdt.circe.testUtil.{converged, diverged, merge}
import io.circe.Json
import org.scalacheck.Prop._
import org.scalacheck.Properties

object NodeToJsonFigure2Spec extends Properties("NodeToJsonFigure2Spec") {
  val colors = doc.downField("colors")
  val p0 =
    Replica.empty("p").applyCmd(colors.downField("blue") := "#0000ff")
  val q0 = merge(Replica.empty("q"), p0)

  property("initial state") = secure {
    converged(p0, q0)
  }

  val p1 = p0.applyCmd(colors.downField("red") := "#ff0000")
  val q1 = q0
    .applyCmd(colors := `{}`)
    .applyCmd(colors.downField("green") := "#00ff00")

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
      "colors" -> Json.obj(
        "red" -> Json.fromString("#ff0000"),
        "green" -> Json.fromString("#00ff00")
      )
    )
  }

  property("toJson with preserve-all-as-array conflict resolution") = secure {
    implicit val resolver = RegNodeConflictResolver.PreserveAllAsArray
    p2.document.toJson ?= Json.obj(
      "colors" -> Json.obj(
        "red" -> Json.arr(Json.fromString("#ff0000")),
        "green" -> Json.arr(Json.fromString("#00ff00"))
      )
    )
  }
}
