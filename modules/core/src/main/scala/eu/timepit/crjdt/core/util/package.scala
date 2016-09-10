package eu.timepit.crjdt.core

import cats.Foldable

package object util {
  final def removeOrUpdate[K, F[_], A](map: Map[K, F[A]], key: K, value: F[A])(
      implicit F: Foldable[F]): Map[K, F[A]] =
    if (F.isEmpty(value)) map - key else map.updated(key, value)
}
