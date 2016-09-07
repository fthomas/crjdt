package eu.timepit.crjdt.core

import eu.timepit.crjdt.core.Key.{DocK, HeadK, IdK, StrK}
import eu.timepit.crjdt.core.TypeTag.{ListT, MapT, RegT}
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

  implicit val valArbitrary: Arbitrary[Val] = {
    val numGen = Arbitrary.arbitrary[BigDecimal].map(Val.Num.apply)
    val strGen = Arbitrary.arbitrary[String].map(Val.Str.apply)
    val constantsGen =
      Gen.oneOf(Val.True, Val.False, Val.Null, Val.EmptyList, Val.EmptyMap)
    Arbitrary(Gen.oneOf(numGen, strGen, constantsGen))
  }
}
