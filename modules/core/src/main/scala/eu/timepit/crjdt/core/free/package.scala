package eu.timepit.crjdt.core

import cats.free.Free

package object free {
  type Cmd[A] = Free[CmdOp, A]

  // workaround for https://issues.scala-lang.org/browse/SI-7139
  val Cmd: CmdCompanion.type = CmdCompanion
}
