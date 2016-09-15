package eu.timepit.crjdt.core

import eu.timepit.crjdt.core.syntax._
import org.scalacheck.Prop._
import org.scalacheck.Properties

object ApplyCmdSpecJvm extends Properties("ReplicaState.applyCmd") {
  val state = ReplicaState.empty("p")

  property("stack safety") = secure {
    val cmd = doc := `{}`
    val many = Iterator.iterate(cmd)(_ `;` cmd).drop(10000).next()
    //state.applyCmd(many)
    true
  }
}
