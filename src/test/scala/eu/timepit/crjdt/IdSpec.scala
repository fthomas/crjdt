package eu.timepit.crjdt

import cats.kernel.laws.OrderLaws
import eu.timepit.crjdt.testInstances._
import org.scalacheck.Properties

class IdSpec extends Properties("Id") {
  include(OrderLaws[Id].order.all)
}
