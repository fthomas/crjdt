package eu.timepit.crjdt.core
package examples

import eu.timepit.crjdt.core.Key.{DocK, IdK, StrK}
import eu.timepit.crjdt.core.ListRef.{HeadR, IdR, TailR}
import eu.timepit.crjdt.core.Node.{ListNode, MapNode, RegNode}
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

  property("content") = secure {
    val todoLists = ListNode(
      Map(
        MapT(IdK(Id(1, "p"))) -> MapNode(
          Map(RegT(StrK("title")) -> RegNode(Map()),
              RegT(StrK("done")) -> RegNode(Map(Id(4, "q") -> Val.True))),
          Map(StrK("done") -> Set(Id(4, "q"))))),
      Map(IdK(Id(1, "p")) -> Set(Id(4, "q"))),
      Map(HeadR -> IdR(Id(1, "p")), IdR(Id(1, "p")) -> TailR))

    p2.document ?= MapNode(
      Map(
        MapT(DocK) -> MapNode(Map(ListT(StrK("todo")) -> todoLists),
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

  property("values") = secure {
    (p2.values(todo.next.downField("done")) ?= List(Val.True)) &&
    (p2.values(todo) ?= List.empty) &&
    (p2.values(doc.downField("foo").iter) ?= List.empty)
  }
}
