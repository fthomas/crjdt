package eu.timepit.crjdt.circe

import eu.timepit.crjdt.core.{Id, LeafVal, Val}
import eu.timepit.crjdt.core.Node.RegNode
import io.circe.Json
import cats.syntax.order._

trait RegNodeConflictResolver {
  def registerToJson(regNode: RegNode): Json

  protected def valToJson(value: LeafVal): Json =
    value match {
      case Val.False  => Json.False
      case Val.True   => Json.True
      case Val.Null   => Json.Null
      case Val.Num(n) => Json.fromBigDecimal(n)
      case Val.Str(s) => Json.fromString(s)
    }
}

object RegNodeConflictResolver {
  implicit object LWW extends RegNodeConflictResolver {
    override def registerToJson(regNode: RegNode): Json = {
      val (_, lastVal) = regNode.regValues.max(new Ordering[(Id, LeafVal)] {
        override def compare(x: (Id, LeafVal), y: (Id, LeafVal)): Int = {
          val (xId, _) = x
          val (yId, _) = y
          xId compare yId
        }
      })
      valToJson(lastVal)
    }
  }

  implicit object PreserveAllAsArray extends RegNodeConflictResolver {
    override def registerToJson(regNode: RegNode): Json = {
      val items = regNode.values.map(valToJson)
      Json.arr(items: _*)
    }
  }
}
