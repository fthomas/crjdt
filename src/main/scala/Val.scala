sealed trait Val extends Product with Serializable

object Val {
  final case class Num(d: BigDecimal)
  final case class Str(s: String)
  case object True extends Val
  case object False extends Val
  case object Null extends Val
  case object EmptyObject extends Val
  case object EmptyArray extends Val
}
