package eu.timepit.crjdt.circe

import eu.timepit.crjdt.core.Replica
import eu.timepit.crjdt.core.syntax._
import eu.timepit.crjdt.circe.syntax._
import eu.timepit.crjdt.circe.testUtil.{converged, diverged, merge}
import io.circe.Json
import org.scalacheck.Prop._
import org.scalacheck.Properties

object NodeToJsonFigure3Spec extends Properties("NodeToJsonFigure3Spec") {

  val p0 = Replica.empty("p")
  val q0 = Replica.empty("q")

  property("initial state") = secure {
    converged(p0, q0)
  }

  val grocery = doc.downField("grocery")

  val p1 = p0
    .applyCmd(grocery := `[]`)
    .applyCmd(grocery.iter.insert("eggs"))
    .applyCmd(grocery.iter.next.insert("ham"))
  val q1 = q0
    .applyCmd(grocery := `[]`)
    .applyCmd(grocery.iter.insert("milk"))
    .applyCmd(grocery.iter.next.insert("flour"))

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
    // Figure 3 shows list of the final state is [“eggs”, “ham”, “milk”, “flour”]
    // but ["milk", "flour", "eggs", "ham"] is also valid order.
    q2.document.toJson ?= Json.obj(
      "grocery" -> Json.arr(
        List("milk", "flour", "eggs", "ham").map(Json.fromString): _*
      )
    )
  }

  property("toJson with preserve-all-as-array conflict resolution") = secure {
    import RegNodeConflictResolver.PreserveAllAsArray
    q2.document.toJson ?= Json.obj(
      "grocery" -> Json.arr(
        List("milk", "flour", "eggs", "ham").map(v =>
          Json.arr(Json.fromString(v))): _*
      )
    )
  }
}
