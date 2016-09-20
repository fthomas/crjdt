package eu.timepit.crjdt.core

import org.scalacheck.Prop
import org.scalacheck.Prop._

import scala.util.Random

object testUtil {
  def converged(a: ReplicaState, b: ReplicaState): Prop =
    (a.processedOps ?= b.processedOps) && (a.context ?= b.context)

  def converged(a: ReplicaState, b: ReplicaState, c: ReplicaState): Prop =
    converged(a, b) && converged(b, c)

  def diverged(a: ReplicaState, b: ReplicaState): Prop =
    (a.processedOps != b.processedOps) && (a.context != b.context)

  def merge(a: ReplicaState, b: ReplicaState): ReplicaState =
    a.applyRemoteOps(b.generatedOps)

  def randomPermutation[A](xs: Vector[A]): Vector[A] = {
    val permutations = xs.permutations.toStream.take(12)
    val index = Random.nextInt(permutations.size)
    permutations.lift(index).getOrElse(Vector.empty)
  }
}
