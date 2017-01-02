package eu.timepit.crjdt.circe

import eu.timepit.crjdt.core.Replica
import eu.timepit.crjdt.core.syntax._
import eu.timepit.crjdt.circe.syntax._
import eu.timepit.crjdt.circe.testUtil.{converged, diverged, merge}
import io.circe.Json
import org.scalacheck.Prop._
import org.scalacheck.Properties

object NodeToJsonFigure4Spec extends Properties("NodeToJsonFigure4Spec") {

  val todo = doc.downField("todo").iter
  val cmd = todo.insert(`{}`) `;`
      (todo.next.downField("title") := "buy milk") `;`
      (todo.next.downField("done") := false)

  val p0 = Replica.empty("p").applyCmd(cmd)
  val q0 = merge(Replica.empty("q"), p0)

  property("initial state") = secure {
    converged(p0, q0)
  }

  val p1 = p0.applyCmd(todo.next.delete)
  val q1 = q0.applyCmd(todo.next.downField("done") := true)

  property("divergence") = secure {
    diverged(p1, q1)
  }

  val p2 = merge(p1, q1)
  val q2 = merge(q1, p1)

  property("convergence") = secure {
    converged(p2, q2)
  }

  property("toJson with last-writer-wins conflict resolution") = secure {
    import RegNodeConflictResolver.LWW
    p2.document.toJson ?= Json.obj(
      "todo" -> Json.arr(
        Json.obj(
          "done" -> Json.True
        )
      )
    )
  }

  property("toJson with preserve-all-as-array conflict resolution") = secure {
    import RegNodeConflictResolver.PreserveAllAsArray
    p2.document.toJson ?= Json.obj(
      "todo" -> Json.arr(
        Json.obj(
          "done" -> Json.arr(Json.True)
        )
      )
    )
  }
}
