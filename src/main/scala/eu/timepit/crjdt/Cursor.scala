package eu.timepit.crjdt

import eu.timepit.crjdt.Cursor.{Key, Tagged}

final case class Cursor(keys: Vector[Tagged[Key]], finalKey: Key)

object Cursor {
  sealed trait Key extends Product with Serializable
  object Key {
    case object DocK extends Key
    case object HeadK extends Key
    final case class StrK(s: String) extends Key
    final case class IdK(id: Id) extends Key
  }

  sealed trait Tagged[A] extends Product with Serializable
  object Tagged {
    final case class MapT[A](value: A) extends Tagged[A]
    final case class ListT[A](value: A) extends Tagged[A]
  }
}
