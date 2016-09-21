package eu.timepit.crjdt.core

import eu.timepit.crjdt.core.Key.{HeadK, IdK}
import eu.timepit.crjdt.core.ListRef.{HeadR, IdR}

sealed trait ListRef extends Product with Serializable

sealed trait KeyRef extends ListRef {
  final def toKey: Key =
    this match {
      case IdR(id) => IdK(id)
      case HeadR => HeadK
    }
}

object ListRef {
  final case class IdR(id: Id) extends KeyRef
  case object HeadR extends KeyRef
  case object TailR extends ListRef

  final def fromKey(key: Key): ListRef =
    key match {
      case IdK(id) => IdR(id)
      case HeadK => HeadR
      case _ => TailR
    }
}
