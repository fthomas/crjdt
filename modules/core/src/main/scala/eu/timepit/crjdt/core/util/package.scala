package eu.timepit.crjdt.core

package object util {
  final def deleteOrUpdate[K, A](map: Map[K, Set[A]],
                                 key: K,
                                 value: Set[A]): Map[K, Set[A]] =
    if (value.isEmpty) map - key else map.updated(key, value)
}
