package eu.timepit.crjdt

import eu.timepit.crjdt.Context.Tagged.{ListT, MapT, RegT}
import eu.timepit.crjdt.Operation.Mutation.AssignM
import eu.timepit.crjdt.Val.{EmptyList, EmptyMap}

sealed trait Context extends Product with Serializable {
  def applyOp(op: Operation): Context =
    op.cur match {
      case Cursor(Vector(), kn) =>
        op.mut match {
          case AssignM(EmptyMap) => // EMPTY-MAP
            val tagged = MapT(Context.Key.from(kn))
            ???
          case AssignM(EmptyList) => // EMPTY-LIST
            val tagged = ListT(Context.Key.from(kn))
            ???
          case AssignM(v) => // ASSIGN
            val tagged = RegT(Context.Key.from(kn))
            ???
          case _ =>
            ???
        }
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

    def from(key: Cursor.Key): Key =
      key match {
        case Cursor.Key.DocK => DocK
        case Cursor.Key.HeadK => HeadK
        case Cursor.Key.IdK(id) => IdK(id)
        case Cursor.Key.StrK(str) => StrK(str)
      }
  }

  sealed trait Value
  object Value {
    final case class ValV(v: Val) extends Value
    final case class IdV(id: Id) extends Value
    case object Tail extends Value
  }

  sealed trait Tagged extends Product with Serializable
  object Tagged {
    final case class MapT(key: Key) extends Tagged
    final case class ListT(key: Key) extends Tagged
    final case class RegT(key: Key) extends Tagged
    final case class Next(key: Key) extends Tagged
  }
}
