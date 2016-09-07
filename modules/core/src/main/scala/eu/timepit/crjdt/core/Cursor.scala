package eu.timepit.crjdt.core

/** `Cursor` identifies a position in a `[[Context]]`. */
final case class Cursor(keys: Vector[BranchTag], finalKey: Key) {
  def append(tag: Key => BranchTag, newFinalKey: Key): Cursor =
    Cursor(keys :+ tag(finalKey), newFinalKey)

  def dropFirst: Cursor =
    copy(keys = keys.drop(1))
}

object Cursor {
  def doc: Cursor =
    withFinalKey(Key.DocK)

  def withFinalKey(finalKey: Key): Cursor =
    Cursor(Vector.empty, finalKey)
}
