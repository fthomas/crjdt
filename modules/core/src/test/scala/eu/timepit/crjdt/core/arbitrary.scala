package eu.timepit.crjdt.core

import eu.timepit.crjdt.core.Expr._
import eu.timepit.crjdt.core.Key.{DocK, HeadK, IdK, StrK}
import eu.timepit.crjdt.core.Operation.Mutation
import eu.timepit.crjdt.core.Operation.Mutation.{AssignM, DeleteM, InsertM}
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
    val docGen = Gen.const(DocK)
    val headGen = Gen.const(HeadK)
    val idGen = Arbitrary.arbitrary[Id].map(IdK.apply)
    val strGen = Arbitrary.arbitrary[String].map(StrK.apply)
    Arbitrary(Gen.oneOf(docGen, headGen, idGen, strGen))
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

  implicit val mutationArbitrary: Arbitrary[Mutation] = {
    val assignGen = Arbitrary.arbitrary[Val].map(AssignM.apply)
    val insertGen = Arbitrary.arbitrary[Val].map(InsertM.apply)
    val deleteGen = Gen.const(DeleteM)
    Arbitrary(Gen.oneOf(assignGen, insertGen, deleteGen))
  }

  implicit val exprArbitrary: Arbitrary[Expr] = {
    val docGen = Gen.const(Doc)
    val varGen = Arbitrary.arbitrary[String].map(Var.apply)
    val downFieldGen = Gen.lzy(for {
      expr <- Arbitrary.arbitrary[Expr]
      key <- Arbitrary.arbitrary[String]
    } yield DownField(expr, key))
    val iterGen = Gen.lzy(Arbitrary.arbitrary[Expr].map(Iter.apply))
    val nextGen = Gen.lzy(Arbitrary.arbitrary[Expr].map(Next.apply))
    Arbitrary(Gen.oneOf(docGen, varGen, downFieldGen, iterGen, nextGen))
  }
}
