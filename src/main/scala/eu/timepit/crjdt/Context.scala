package eu.timepit.crjdt

sealed trait Context extends Product with Serializable {
  def applyOp(op: Operation): Context =
    op.cur match {
      case Cursor(Vector(), Cursor.Key.DocK) => ???
      case Cursor(ks, kn) => ???
    }
}

object Context {
  def empty: Context = Empty

  case object Empty extends Context
  final case class Leaf(value: Value) extends Context
  final case class Assoc(key: Tagged, ctx: Context, pres: Set[Id])
      extends Context
  final case class Many(assocs: List[Assoc]) extends Context

  // Is this the same as Cursor.Key?
  sealed trait Key extends Product with Serializable
  object Key {
    case object DocK extends Key
    case object HeadK extends Key
    final case class IdK(id: Id) extends Key
    final case class StrK(str: String) extends Key
  }

  sealed trait Value
  object Value {
    final case class ValV(v: Val) extends Value
    final case class IdV(id: Id) extends Value
  }

  sealed trait Tagged extends Product with Serializable
  object Tagged {
    final case class MapT(key: Key) extends Tagged
    final case class ListT(key: Key) extends Tagged
    final case class RegT(key: Key) extends Tagged
    final case class Next(key: Key) extends Tagged
  }
}
