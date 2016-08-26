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
    final case class IdK(id: Id) extends Key
    final case class StrK(str: String) extends Key
  }

  sealed trait Tagged extends Product with Serializable
  object Tagged {
    final case class MapT(key: Key) extends Tagged
    final case class ListT(key: Key) extends Tagged
  }

  def withFinalKey(finalKey: Key): Cursor =
    Cursor(Vector.empty, finalKey)
}
