package eu.timepit.crjdt.circe

import eu.timepit.crjdt.core.{Cmd, Expr, Replica, Val}
import eu.timepit.crjdt.core.syntax._
import io.circe.{Json, JsonObject}
import org.scalacheck.Prop
import org.scalacheck.Prop._

import scala.util.Random

object testUtil {
  def converged(a: Replica, b: Replica): Prop =
    (a.processedOps ?= b.processedOps) && (a.document ?= b.document)

  def converged(a: Replica, b: Replica, c: Replica): Prop =
    converged(a, b) && converged(b, c)

  def diverged(a: Replica, b: Replica): Prop =
    (a.processedOps != b.processedOps) && (a.document != b.document)

  def merge(a: Replica, b: Replica): Replica =
    a.applyRemoteOps(b.generatedOps)

  def randomPermutation[A](xs: Vector[A]): Vector[A] = {
    val permutations = xs.permutations.toStream.take(12)
    val index = Random.nextInt(permutations.size)
    permutations.lift(index).getOrElse(Vector.empty)
  }

  def assignCmds(expr: Expr, value: Json): Vector[Cmd] = {
    val assign = expr := jsonToVal(value)
    val fillEmptyArrayOrMap = value.arrayOrObject(
      Vector.empty,
      array => insertToArrayCmds(expr, array),
      obj => assignObjectFieldsCmds(expr, obj)
    )
    assign +: fillEmptyArrayOrMap
  }

  def insertToArrayCmds(expr: Expr, array: Vector[Json]): Vector[Cmd] = {
    val (_, commands) = array.foldLeft((expr.iter.next, Vector.empty[Cmd])) {
      (acc, item) =>
        val (position, commands) = acc
        val insert = position.insert(jsonToVal(item))
        val fillEmptyArrayOrMap = item.arrayOrObject(
          Vector.empty,
          array => insertToArrayCmds(position, array),
          obj => assignObjectFieldsCmds(position, obj)
        )
        (position.next, commands ++ (insert +: fillEmptyArrayOrMap))
    }
    commands
  }

  def assignObjectFieldsCmds(expr: Expr, obj: JsonObject): Vector[Cmd] =
    obj.toMap.flatMap {
      case (key, value) =>
        val field = expr.downField(key)
        assignCmds(field, value)
    }.toVector

  def jsonToVal(value: Json): Val =
    value.fold(
      Val.Null,
      bool => if (bool) Val.True else Val.False,
      number => Val.Num(number.toBigDecimal.getOrElse(number.toDouble)),
      string => Val.Str(string),
      array => Val.EmptyList,
      obj => Val.EmptyMap
    )
}
