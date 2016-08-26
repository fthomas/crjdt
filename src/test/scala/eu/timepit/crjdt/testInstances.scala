package eu.timepit.crjdt

import org.scalacheck.Arbitrary

object testInstances {
  implicit val idArbitrary: Arbitrary[Id] = {
    val gen = for {
      c <- Arbitrary.arbitrary[BigInt]
      p <- Arbitrary.arbitrary[String]
    } yield Id(c, p)
    Arbitrary(gen)
  }
}
