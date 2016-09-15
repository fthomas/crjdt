package eu.timepit.crjdt.core
package examples

import eu.timepit.crjdt.core.Context.{ListCtx, MapCtx, RegCtx}
import eu.timepit.crjdt.core.Key.{DocK, IdK, StrK}
import eu.timepit.crjdt.core.ListRef.{HeadR, IdR, TailR}
import eu.timepit.crjdt.core.TypeTag.{ListT, MapT, RegT}
import eu.timepit.crjdt.core.syntax._
import org.scalacheck.Prop._
import org.scalacheck.Properties

object Figure4 extends Properties("Figure4") {
  val todo = doc.downField("todo").iter
  val cmd = todo.insert(`{}`) `;`
      (todo.next.downField("title") := "buy milk") `;`
      (todo.next.downField("done") := false)

  val p0 = ReplicaState.empty("p").applyCmd(cmd)
  val q0 = ReplicaState.empty("q").applyRemoteOps(p0.generatedOps)

  property("initial state") = secure {
    p0.context ?= q0.context
  }

  val p1 = p0.applyCmd(todo.next.delete)
  val q1 = q0.applyCmd(todo.next.downField("done") := true)

  property("divergence") = secure {
    p1.context != q1.context
  }

  val p2 = p1.applyRemoteOps(q1.generatedOps)
  val q2 = q1.applyRemoteOps(p1.generatedOps)

  property("convergence") = secure {
    p2.context ?= q2.context
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
}
