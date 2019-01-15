package eu.timepit.crjdt.core

import cats.Foldable
import cats.UnorderedFoldable

package object util {
  final def applyAllLeft[F[_], A](fs: F[A => A], init: A)(
      implicit F: Foldable[F]): A =
    F.foldLeft(fs, init) { case (a, f) => f(a) }

  /** Removes `key` from `map` if `value` is empty or else adds the
    * key-value pair to `map`.
    */
  final def removeOrUpdate[K, F[_], A](map: Map[K, F[A]], key: K, value: F[A])(
      implicit F: UnorderedFoldable[F]): Map[K, F[A]] =
    if (F.isEmpty(value)) map - key else map.updated(key, value)
}
