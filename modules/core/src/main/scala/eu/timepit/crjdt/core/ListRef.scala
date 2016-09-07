package eu.timepit.crjdt.core

import eu.timepit.crjdt.core.Key.{HeadK, IdK}
import eu.timepit.crjdt.core.ListRef.{HeadR, IdR}

sealed trait ListRef extends Product with Serializable {
  final def toKey: Key =
    this match {
      case IdR(id) => IdK(id)
      case HeadR => HeadK
      case _ => HeadK
    }
}

object ListRef {
  final case class IdR(id: Id) extends ListRef
  case object HeadR extends ListRef
  case object TailR extends ListRef

  def fromKey(key: Key): ListRef =
    key match {
      case IdK(id) => IdR(id)
      case HeadK => HeadR
      case _ => TailR
    }
}
