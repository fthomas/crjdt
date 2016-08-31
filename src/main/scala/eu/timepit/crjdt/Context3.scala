package eu.timepit.crjdt

import eu.timepit.crjdt.Context3.Tagged.{ListT, MapT, RegT}
import eu.timepit.crjdt.Operation.Mutation.{AssignM, DeleteM, InsertM}
import eu.timepit.crjdt.Val.{EmptyList, EmptyMap}

sealed trait Context3 extends Product with Serializable {
  def applyOp(op: Operation): Context3 =
    op.cur match {
      case Cursor(Vector(), kn) =>
        op.mut match {
          case AssignM(EmptyMap) => // EMPTY-MAP
            // 1. clear prior value at cursor
            // 2. adding op.id to presence set
            // 3. add new value to ctx
            val tagged = MapT(kn)
            ???
          case AssignM(EmptyList) => // EMPTY-LIST
            val tagged = ListT(kn)
            ???
          case AssignM(v) => // ASSIGN
            val tagged = RegT(kn)
            ???
          case InsertM(v) => // INSERT1, INSERT2
            ???
          case DeleteM => // DELETE
            ???
        }
      case Cursor(ks, kn) => ???
    }
}

object Context3 {
  def empty: Context3 = Empty

  case object Empty extends Context3
  final case class Leaf(value: Value) extends Context3
  final case class Assoc(key: Tagged, ctx: Context3, pres: Set[Id])
      extends Context3
  final case class Many(assocs: List[Assoc]) extends Context3

  /*
  data Context =
      Empty
    | X Tagged Context Pres // map
    |                       // list
    | regT(k) -> RegValues, pres(k) -> {}  // register
   */

  sealed trait Value
  object Value {
    final case class ValV(v: Val) extends Value
    final case class IdV(id: Id) extends Value
    case object Tail extends Value
  }

  // TODO: Replace with Tag
  sealed trait Tagged extends Product with Serializable
  object Tagged {
    final case class MapT(key: Key) extends Tagged
    final case class ListT(key: Key) extends Tagged
    final case class RegT(key: Key) extends Tagged
    final case class Next(key: Key) extends Tagged
  }
}
