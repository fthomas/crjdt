package eu.timepit.crjdt.core

import cats.implicits._
import cats.kernel.laws.discipline.OrderTests
import eu.timepit.crjdt.core.arbitrary._
import org.scalacheck.Properties

object IdSpec extends Properties("Id") {
  include(OrderTests[Id].order.all)
}
