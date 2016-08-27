package eu.timepit.crjdt

import eu.timepit.crjdt.Cursor.Tagged

final case class Cursor(keys: Vector[Tagged], finalKey: Key) {
  def push(tag: Key => Tagged, newFinalKey: Key): Cursor =
    Cursor(keys :+ tag(finalKey), newFinalKey)
}

object Cursor {
  sealed trait Tagged extends Product with Serializable
  object Tagged {
    final case class MapT(key: Key) extends Tagged
    final case class ListT(key: Key) extends Tagged
  }

  def withFinalKey(finalKey: Key): Cursor =
    Cursor(Vector.empty, finalKey)
}
