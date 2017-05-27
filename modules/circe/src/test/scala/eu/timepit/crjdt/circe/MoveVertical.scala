package eu.timepit.crjdt.circe

import eu.timepit.crjdt.circe.RegNodeConflictResolver.LWW
import eu.timepit.crjdt.circe.syntax._
import eu.timepit.crjdt.core.syntax._
import eu.timepit.crjdt.core.{After, Before, Cmd, Replica}
import io.circe.Json
import org.scalacheck.Prop._
import org.scalacheck.Properties

object MoveVertical extends Properties("MoveVertical") {

  val children = doc.downField("children")
  val head = children.iter
  val one = children.iter.next
  val two = children.iter.next.next
  val three = children.iter.next.next.next
  val four = children.iter.next.next.next.next
  val five = children.iter.next.next.next.next.next

  def testConcurrentOps(resultList: List[String], cmdLists: List[Cmd]*) = {
    val start = Replica
      .empty("start")
      .applyCmd(children := `[]`)
      .applyCmd(head.insert("1"))
      .applyCmd(one.insert("2"))
      .applyCmd(two.insert("3"))
      .applyCmd(three.insert("4"))
      .applyCmd(four.insert("5"))

    val replicas0 = for (i <- cmdLists.indices) yield {
      Replica.empty(i.toString).applyRemoteOps(start.generatedOps)
    }

    val replicas1 = for (i <- cmdLists.indices) yield {
      replicas0(i).applyCmds(cmdLists(i))
    }

    val replicas2 = for (i <- cmdLists.indices) yield {
      val othersOpLists = for (j <- cmdLists.indices if j != i)
        yield replicas1(j).generatedOps
      replicas1(i).applyRemoteOps(othersOpLists.flatten.to[Vector])
    }

    property("converged") = secure {
      val props = for (replica <- replicas2) yield {
        (replicas2(0).processedOps ?= replica.processedOps) &&
        (replicas2(0).document ?= replica.document)
      }
      all(props: _*)
    }

    property("content") = secure {
      val props = for (replica <- replicas2) yield {
        replica.document.toJson ?= Json.obj(
          "children" -> Json.arr(resultList.map(Json.fromString): _*)
        )
      }
      all(props: _*)
    }
  }

  /* Note: When two ops are concurrent, the op whose replica name is later in
   * the alphabet will be first. */

  testConcurrentOps(List("2", "3", "5", "1", "4"),
                    List(one.moveVertical(three, After)),
                    List(five.moveVertical(one, Before)))

  val ins0 = "User0 inserted after 1"
  val ins1 = "User1 inserted after 1"
  testConcurrentOps(List("1", ins1, ins0, "2", "3", "4", "5"),
                    List(one.insert(ins0)),
                    List(one.insert(ins1)))

  // three users do an op concurrently
  testConcurrentOps(List("1", ins1, ins0, "2", "3", "5", "4"),
                    List(four.moveVertical(five, After)),
                    List(one.insert(ins0)),
                    List(one.insert(ins1)))

  // three users do an op concurrently
  testConcurrentOps(List("1", ins1, ins0, "2", "3", "5", "4"),
                    List(one.insert(ins0)),
                    List(one.insert(ins1)),
                    List(four.moveVertical(five, After)))

  // ops where nothing has to be done
  testConcurrentOps(
    List("1", "2", "3", "4", "5"),
    List(one.moveVertical(two, Before), two.moveVertical(one, After)),
    List(one.moveVertical(one, Before)))

  testConcurrentOps(List("5", "2", "3", "4"),
                    List(one.delete),
                    List(five.moveVertical(one, After)))

  testConcurrentOps(List("5", "2", "3", "4"),
                    List(five.moveVertical(one, After)),
                    List(one.delete))

  // todo: after topological sort is implemented,
  // move five would be done after move one, so the result would be
  // List("2", "3", "5", "1", "4") instead of:
  testConcurrentOps(List("5", "2", "3", "1", "4"),
                    List(five.moveVertical(one, Before)),
                    List(one.moveVertical(three, After)))

  // todo: after topological sort is implemented,
  // move would be done after insert, so the result would be
  // List("2", "3", "4", "1", "Inserted after 1", "5") instead of:
  testConcurrentOps(List("Inserted after 1", "2", "3", "4", "1", "5"),
                    List(one.insert("Inserted after 1")),
                    List(one.moveVertical(four, After)))

  // todo: ideally, the two moves would be considered concurrent so the result
  // would be List("2", "4", "Renamed 1", "3", "5") instead of:
  testConcurrentOps(List("3", "2", "4", "Renamed 1", "5"),
                    List(one := "Renamed 1", one.moveVertical(four, After)),
                    List(three.moveVertical(one, After)))
}
