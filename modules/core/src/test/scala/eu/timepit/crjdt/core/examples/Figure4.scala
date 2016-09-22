package eu.timepit.crjdt.core
package examples

import eu.timepit.crjdt.core.Context.{ListCtx, MapCtx, RegCtx}
import eu.timepit.crjdt.core.Key.{DocK, IdK, StrK}
import eu.timepit.crjdt.core.ListRef.{HeadR, IdR, TailR}
import eu.timepit.crjdt.core.TypeTag.{ListT, MapT, RegT}
import eu.timepit.crjdt.core.syntax._
import eu.timepit.crjdt.core.testUtil._
import org.scalacheck.Prop._
import org.scalacheck.Properties

object Figure4 extends Properties("Figure4") {
  val todo = doc.downField("todo").iter
  val cmd = todo.insert(`{}`) `;`
      (todo.next.downField("title") := "buy milk") `;`
      (todo.next.downField("done") := false)

  val p0 = ReplicaState.empty("p").applyCmd(cmd)
  val q0 = merge(ReplicaState.empty("q"), p0)

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

  property("content") = secure {
    val todoLists = ListCtx(
      Map(
        MapT(IdK(Id(1, "p"))) -> MapCtx(
          Map(RegT(StrK("title")) -> RegCtx(Map()),
              RegT(StrK("done")) -> RegCtx(Map(Id(4, "q") -> Val.True))),
          Map(StrK("done") -> Set(Id(4, "q"))))),
      Map(IdK(Id(1, "p")) -> Set(Id(4, "q"))),
      Map(HeadR -> IdR(Id(1, "p")), IdR(Id(1, "p")) -> TailR))

    p2.context ?= MapCtx(
      Map(
        MapT(DocK) -> MapCtx(Map(ListT(StrK("todo")) -> todoLists),
                             Map(
                               StrK("todo") -> Set(Id(1, "p"),
                                                   Id(2, "p"),
                                                   Id(3, "p"),
                                                   Id(4, "q"))))),
      Map(DocK -> Set(Id(1, "p"), Id(2, "p"), Id(3, "p"), Id(4, "q"))))
  }

  property("keys") = secure {
    (p2.keys(doc) ?= Set("todo")) &&
    (p2.keys(todo.next) ?= Set("done")) &&
    (p2.keys(todo) ?= Set.empty) &&
    (p2.keys(doc.downField("foo").iter) ?= Set.empty)
  }
}
