package eu.timepit.crjdt.core

/** `Cursor` identifies a position in a `[[Node]]`. */
final case class Cursor(keys: Vector[BranchTag], finalKey: Key) {
  def append(tag: Key => BranchTag, newFinalKey: Key): Cursor =
    Cursor(keys :+ tag(finalKey), newFinalKey)

  def view: Cursor.View =
    keys match {
      case k1 +: kn => Cursor.Branch(k1, Cursor(kn, finalKey))
      case _ => Cursor.Leaf(finalKey)
    }
}

object Cursor {
  sealed trait View extends Product with Serializable
  final case class Leaf(finalKey: Key) extends View
  final case class Branch(head: BranchTag, tail: Cursor) extends View

  final def doc: Cursor =
    withFinalKey(Key.DocK)

  final def withFinalKey(finalKey: Key): Cursor =
    Cursor(Vector.empty, finalKey)
}
