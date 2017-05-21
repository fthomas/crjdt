package eu.timepit.crjdt.circe

import eu.timepit.crjdt.circe.RegNodeConflictResolver.LWW
import eu.timepit.crjdt.circe.syntax._
import eu.timepit.crjdt.core.syntax._
import eu.timepit.crjdt.core.{After, Before, Cmd, Replica}
import io.circe.Json
import org.scalacheck.Prop._
import org.scalacheck.Prop.secure
import org.scalacheck.Properties
import wvlet.log.LogLevel.{OFF}
import wvlet.log.LogSupport

object MoveVertical extends Properties("MoveVertical") with LogSupport {

  wvlet.log.Logger.setDefaultLogLevel(OFF)

  val children = doc.downField("children")
  val head = children.iter
  val one = children.iter.next
  val two = children.iter.next.next
  val three = children.iter.next.next.next
  val four = children.iter.next.next.next.next
  val five = children.iter.next.next.next.next.next

  def testConcurrentOps(aliceOps: List[Cmd],
                        bobsOps: List[Cmd],
                        resultList: List[String]) = {
    val alice0 = Replica
      .empty("Alice")
      .applyCmd(children := `[]`)
      .applyCmd(head.insert("1"))
      .applyCmd(one.insert("2"))
      .applyCmd(two.insert("3"))
      .applyCmd(three.insert("4"))
      .applyCmd(four.insert("5"))

    debug("alice0 / bob0:\n" + alice0.document)
    info("alice0 / bob0 json:\n" + alice0.document.toJson)

    val bob0 = Replica.empty("Bob").applyRemoteOps(alice0.generatedOps)

    val alice1 = alice0.applyCmds(aliceOps)
    val bob1 = bob0.applyCmds(bobsOps)
    info("alice1 json:\n" + alice1.document.toJson)
    info("bob1 json:\n" + bob1.document.toJson)

    val alice2 = alice1.applyRemoteOps(bob1.generatedOps)
    val bob2 = bob1.applyRemoteOps(alice1.generatedOps)
    debug("alice2:\n" + alice2.document)
    debug("bob2:\n" + bob2.document)

    val alice2Json = alice2.document.toJson
    val bob2Json = bob2.document.toJson
    if (alice2Json == bob2Json) {
      info("alice2 == bob2:\n" + alice2Json)
    } else {
      info("alice2 json:\n" + alice2Json)
      info("bob2 json:\n" + bob2Json)
    }

    property("converged") = secure {
      alice2Json ?= Json.obj(
        "children" -> Json.arr(resultList.map(Json.fromString): _*)
      )
    }
  }

  // note: when two ops are concurrent, bobs op is applied before alices op

  testConcurrentOps(
    List(one.moveVertical(three, After)),
    List(five.moveVertical(one, After)),
    List("2", "3", "1", "5", "4")
  )

  testConcurrentOps(
    List(one.moveVertical(three, Before)),
    List(five.moveVertical(one, Before)),
    List("2", "5", "1", "3", "4")
  )

  testConcurrentOps(
    List(one.moveVertical(three, After)),
    List(five.moveVertical(one, Before)),
    List("2", "3", "5", "1", "4")
  )

//  // here, move five should be done after move one
//  testConcurrentOps(
//    List(five.moveVertical(one, Before)),
//    List(one.moveVertical(three, After)),
//    List("2", "3", "5", "1", "4")
//  )

//  // here, move should be done after insert
//  testConcurrentOps(
//    List(one.insert("Inserted after 1")),
//    List(one.moveVertical(four, After)),
//    List("2", "3", "4", "1", "Inserted after 1", "5")
//  )

//  // here, the two moves should be considered concurrent
//  testConcurrentOps(
//    List(one := "Renamed 1", one.moveVertical(four, After)),
//    List(three.moveVertical(one, After)),
//    List("2", "4", "Renamed 1", "3", "5")
//  )

  testConcurrentOps(
    List(one.delete),
    List(five.moveVertical(one, After)),
    List("5", "2", "3", "4")
  )

  testConcurrentOps(
    List(five.moveVertical(one, After)),
    List(one.delete),
    List("5", "2", "3", "4")
  )
}
