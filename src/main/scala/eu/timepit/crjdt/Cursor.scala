package eu.timepit.crjdt

final case class Cursor(keys: Vector[Any], finalKey: Any)

// key can be:
// * an Id
// * special token: doc, head
// * a string key

// type tag: mapT, listT, regT

sealed trait TypeTag[A]
case class MapT[A](a: A) extends TypeTag[A]
case class ListT[A](a: A) extends TypeTag[A]
//...
