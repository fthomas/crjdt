package eu.timepit.crjdt

sealed trait Tag extends Product with Serializable
sealed trait RecTag extends Tag

object Tag {
  final case class MapT(key: Key) extends RecTag
  final case class ListT(key: Key) extends RecTag
  final case class RegT(key: Key) extends Tag
}
