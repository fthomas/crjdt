package eu.timepit.crjdt.core

import cats.Order

final case class Id(c: BigInt, p: ReplicaId)

object Id {
  implicit val orderId: Order[Id] =
    Order.from { (x: Id, y: Id) =>
      val rc = x.c compare y.c
      if (rc != 0) rc else x.p compareTo y.p
    }
}
