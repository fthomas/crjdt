package eu.timepit.crjdt

sealed trait Key extends Product with Serializable

object Key {
  case object DocK extends Key
  case object HeadK extends Key
  final case class IdK(id: Id) extends Key
  final case class StrK(str: String) extends Key
}
