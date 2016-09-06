package eu.timepit.crjdt

import eu.timepit.crjdt.Key.{DocK, HeadK, IdK, StrK}
import eu.timepit.crjdt.TypeTag.{ListT, MapT, RegT}
import org.scalacheck.{Arbitrary, Gen}

object arbitrary {
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

  implicit val branchTagArbitrary: Arbitrary[BranchTag] = {
    val gen = Arbitrary.arbitrary[Key].flatMap { key =>
      Gen.oneOf(MapT(key), ListT(key))
    }
    Arbitrary(gen)
  }

  implicit val typeTagArbitrary: Arbitrary[TypeTag] = {
    val gen = Gen.oneOf(Arbitrary.arbitrary[Key].map(RegT.apply),
                        Arbitrary.arbitrary[BranchTag])
    Arbitrary(gen)
  }

  implicit val cursorArbitrary: Arbitrary[Cursor] = {
    val gen = for {
      keys <- Arbitrary.arbitrary[Vector[BranchTag]]
      finalKey <- Arbitrary.arbitrary[Key]
    } yield Cursor(keys, finalKey)
    Arbitrary(gen)
  }
}
