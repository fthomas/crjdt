package eu.timepit.crjdt.core

import cats.Eq
import cats.Order

final case class Id(c: BigInt, p: ReplicaId)

object Id {
  implicit final val orderEq: Eq[Id] =
    Eq.fromUniversalEquals

  implicit final val orderId: Order[Id] =
    Order.from { (x: Id, y: Id) =>
      val rc = x.c compare y.c
      if (rc != 0) rc else x.p compareTo y.p
    }

  implicit final val orderingId: Ordering[Id] =
    Order[Id].toOrdering
}
