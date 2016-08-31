package eu.timepit.crjdt

final case class Cursor(keys: Vector[RecTag], finalKey: Key) {
  def dropFirst: Cursor =
    copy(keys = keys.drop(1))

  def push(tag: Key => RecTag, newFinalKey: Key): Cursor =
    Cursor(keys :+ tag(finalKey), newFinalKey)
}

object Cursor {
  def doc: Cursor =
    withFinalKey(Key.DocK)

  def withFinalKey(finalKey: Key): Cursor =
    Cursor(Vector.empty, finalKey)
}
