package eu.timepit.crjdt.circe

import eu.timepit.crjdt.circe.RegNodeConflictResolver.LWW
import eu.timepit.crjdt.circe.syntax._
import eu.timepit.crjdt.core.syntax._
import eu.timepit.crjdt.core.{After, Replica}
import org.scalacheck.Properties

object MoveVertical extends Properties("MoveVertical") {

  // when done concurrently, q is applied before p

  def merge(a: Replica, b: Replica): Replica =
    a.applyRemoteOps(b.generatedOps)

  val grocery = doc.downField("grocery")
  val eins = grocery.iter.next.next
  val vier = grocery.iter.next.next.next.next.next
  val fuenf = grocery.iter.next.next.next.next.next.next
  val sieben = grocery.iter.next.next.next.next.next.next.next.next

  val p0 = Replica
    .empty("p")
    .applyCmd(grocery := `[]`)
    .applyCmd(grocery.iter.insert("null"))
    .applyCmd(grocery.iter.next.insert("eins"))
    .applyCmd(grocery.iter.next.next.insert("zwei"))
    .applyCmd(grocery.iter.next.next.next.insert("drei"))
    .applyCmd(grocery.iter.next.next.next.next.insert("vier"))
    .applyCmd(grocery.iter.next.next.next.next.next.insert("fünf"))
    .applyCmd(grocery.iter.next.next.next.next.next.next.insert("sechs"))
    .applyCmd(grocery.iter.next.next.next.next.next.next.next.insert("sieben"))

  println("p0:" + p0.document)
  println("p0 json:" + p0.document.toJson)

  val q0 = merge(Replica.empty("q"), p0)

  val p1 = p0.applyCmd(vier.moveVertical(eins, After))
  val q1 = q0.applyCmd(fuenf := "FUENF UMBENANNT")
  val q15 = q1.applyCmd(grocery.iter.next.next.next.insert("INS NACH ZWEI"))
  val q2 = q15.applyCmd(sieben.moveVertical(eins, After))

  println("q1 json:" + q2.document.toJson)

  val p3 = merge(p1, q2)
  val q3 = merge(q2, p1)

  println("p3:" + p3.document)
  println("p3 json:" + p3.document.toJson)
  println("q3:" + q3.document)
  println("q3 json:" + q3.document.toJson)
}
