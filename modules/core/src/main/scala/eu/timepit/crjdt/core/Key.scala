package eu.timepit.crjdt.core

/** `Key` represents the untyped components of a `[[Cursor]]`. */
sealed trait Key extends Product with Serializable

object Key {
  case object DocK extends Key
  case object HeadK extends Key
  final case class IdK(id: Id) extends Key
  final case class StrK(str: String) extends Key
}
