package eu.timepit.crjdt.core

object testUtil {
  def randomPermutation[A](xs: Vector[A]): Vector[A] = {
    val permutations = xs.permutations.toStream
    val index = scala.util.Random.nextInt(10)
    permutations
      .lift(index)
      .orElse(permutations.lastOption)
      .getOrElse(Vector.empty)
  }
}
