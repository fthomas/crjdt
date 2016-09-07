package eu.timepit.crjdt.core

import cats.kernel.laws.OrderLaws
import eu.timepit.crjdt.core.arbitrary._
import org.scalacheck.Properties

object IdSpec extends Properties("Id") {
  include(OrderLaws[Id].order.all)
}
