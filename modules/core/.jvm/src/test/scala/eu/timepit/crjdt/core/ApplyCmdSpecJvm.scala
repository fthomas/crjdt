package eu.timepit.crjdt.core

import eu.timepit.crjdt.core.syntax._
import org.scalacheck.Prop._
import org.scalacheck.Properties

object ApplyCmdSpecJvm extends Properties("ReplicaState.applyCmd") {
  val state = ReplicaState.empty("p")

  property("stack safety") = secure {
    val count = 100000
    val cmd = let(v("x")) = doc
    val many = Iterator.iterate(cmd)(_ `;` cmd).drop(count).next()
    state.applyCmd(many).variables.get(v("x")) ?= Some(Cursor.doc)
  }
}
