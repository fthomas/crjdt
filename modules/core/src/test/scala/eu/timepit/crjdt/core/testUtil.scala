package eu.timepit.crjdt.core

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
}
