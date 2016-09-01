package eu.timepit.crjdt

import eu.timepit.crjdt.Key.{DocK, HeadK, IdK, StrK}
import org.scalacheck.{Arbitrary, Gen}

object testInstances {
  implicit val idArbitrary: Arbitrary[Id] = {
    val gen = for {
      c <- Arbitrary.arbitrary[BigInt]
      p <- Arbitrary.arbitrary[String]
    } yield Id(c, p)
    Arbitrary(gen)
  }

  implicit val keyArbitrary: Arbitrary[Key] = {
    val doc = Gen.const(DocK)
    val head = Gen.const(HeadK)
    val id = Arbitrary.arbitrary[Id].map(IdK.apply)
    val str = Arbitrary.arbitrary[String].map(StrK.apply)
    Arbitrary(Gen.oneOf(doc, head, id, str))
  }
}
