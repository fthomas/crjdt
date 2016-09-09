package eu.timepit.crjdt.core

import eu.timepit.crjdt.core.Cmd._
import eu.timepit.crjdt.core.Expr._
import eu.timepit.crjdt.core.Key.{DocK, HeadK, IdK, StrK}
import eu.timepit.crjdt.core.Operation.Mutation
import eu.timepit.crjdt.core.Operation.Mutation.{AssignM, DeleteM, InsertM}
import eu.timepit.crjdt.core.TypeTag.{ListT, MapT, RegT}
import org.scalacheck.{Arbitrary, Gen}

object arbitrary {
  implicit val arbitraryId: Arbitrary[Id] = {
    val gen = for {
      c <- Arbitrary.arbitrary[BigInt]
      p <- Arbitrary.arbitrary[String]
    } yield Id(c, p)
    Arbitrary(gen)
  }

  implicit val arbitraryKey: Arbitrary[Key] = {
    val genDocK = Gen.const(DocK)
    val genHeadK = Gen.const(HeadK)
    val genIdK = Arbitrary.arbitrary[Id].map(IdK.apply)
    val genStrK = Arbitrary.arbitrary[String].map(StrK.apply)
    Arbitrary(Gen.oneOf(genDocK, genHeadK, genIdK, genStrK))
  }

  implicit val arbitraryBranchTag: Arbitrary[BranchTag] = {
    val gen = Arbitrary.arbitrary[Key].flatMap { key =>
      Gen.oneOf(MapT(key), ListT(key))
    }
    Arbitrary(gen)
  }

  implicit val arbitraryTypeTag: Arbitrary[TypeTag] = {
    val gen = Gen.oneOf(Arbitrary.arbitrary[Key].map(RegT.apply),
                        Arbitrary.arbitrary[BranchTag])
    Arbitrary(gen)
  }

  implicit val arbitraryCursor: Arbitrary[Cursor] = {
    val gen = for {
      keys <- Arbitrary.arbitrary[Vector[BranchTag]]
      finalKey <- Arbitrary.arbitrary[Key]
    } yield Cursor(keys, finalKey)
    Arbitrary(gen)
  }

  implicit val arbitraryVal: Arbitrary[Val] = {
    val genNum = Arbitrary.arbitrary[BigDecimal].map(Val.Num.apply)
    val genStr = Arbitrary.arbitrary[String].map(Val.Str.apply)
    val genConstants =
      Gen.oneOf(Val.True, Val.False, Val.Null, Val.EmptyList, Val.EmptyMap)
    Arbitrary(Gen.oneOf(genNum, genStr, genConstants))
  }

  implicit val arbitraryMutation: Arbitrary[Mutation] = {
    val genAssignM = Arbitrary.arbitrary[Val].map(AssignM.apply)
    val genInsertM = Arbitrary.arbitrary[Val].map(InsertM.apply)
    val genDeleteM = Gen.const(DeleteM)
    Arbitrary(Gen.oneOf(genAssignM, genInsertM, genDeleteM))
  }

  implicit val arbitraryVar: Arbitrary[Var] =
    Arbitrary(Arbitrary.arbitrary[String].map(Var.apply))

  implicit val arbitraryExpr: Arbitrary[Expr] = {
    val genDoc = Gen.const(Doc)
    val genVar = Arbitrary.arbitrary[Var]
    val genDownField = Gen.lzy(for {
      expr <- Arbitrary.arbitrary[Expr]
      key <- Arbitrary.arbitrary[String]
    } yield DownField(expr, key))
    val genIter = Gen.lzy(Arbitrary.arbitrary[Expr].map(Iter.apply))
    val genNext = Gen.lzy(Arbitrary.arbitrary[Expr].map(Next.apply))
    Arbitrary(Gen.oneOf(genDoc, genVar, genDownField, genIter, genNext))
  }

  implicit val arbitraryCmd: Arbitrary[Cmd] = {
    val genLet = for {
      v <- Arbitrary.arbitrary[Var]
      expr <- Arbitrary.arbitrary[Expr]
    } yield Let(v, expr)
    val genAssign = for {
      expr <- Arbitrary.arbitrary[Expr]
      value <- Arbitrary.arbitrary[Val]
    } yield Assign(expr, value)
    val genInsert = for {
      expr <- Arbitrary.arbitrary[Expr]
      value <- Arbitrary.arbitrary[Val]
    } yield Insert(expr, value)
    val genDelete = Arbitrary.arbitrary[Expr].map(Delete.apply)
    val genSequence = Gen.lzy(for {
      cmd1 <- Arbitrary.arbitrary[Cmd]
      cmd2 <- Arbitrary.arbitrary[Cmd]
    } yield Sequence(cmd1, cmd2))
    Arbitrary(Gen.oneOf(genLet, genAssign, genInsert, genDelete, genSequence))
  }
}
