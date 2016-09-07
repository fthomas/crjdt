package eu.timepit.crjdt.core

/** `TypeTag` tags a `[[Key]]` with the data type it refers to. */
sealed trait TypeTag extends Product with Serializable {
  def key: Key
}

/** `BranchTag` denotes data types that can have child nodes. */
sealed trait BranchTag extends TypeTag

object TypeTag {
  final case class MapT(key: Key) extends BranchTag
  final case class ListT(key: Key) extends BranchTag
  final case class RegT(key: Key) extends TypeTag
}
