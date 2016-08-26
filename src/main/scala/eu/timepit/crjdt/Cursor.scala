package eu.timepit.crjdt

import eu.timepit.crjdt.Cursor.{Key, Tagged}

final case class Cursor(keys: Vector[Tagged], finalKey: Key) {
  def push(tag: Key => Tagged, newFinalKey: Key): Cursor =
    Cursor(keys :+ tag(finalKey), newFinalKey)
}

object Cursor {
  sealed trait Key extends Product with Serializable
  object Key {
    case object DocK extends Key
    case object HeadK extends Key
    final case class StrK(str: String) extends Key
    final case class IdK(id: Id) extends Key
  }

  sealed trait Tagged extends Product with Serializable
  object Tagged {
    final case class MapT(value: Key) extends Tagged
    final case class ListT(value: Key) extends Tagged
  }

  def withFinalKey(finalKey: Key): Cursor =
    Cursor(Vector.empty, finalKey)
}
