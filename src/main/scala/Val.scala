sealed trait Val extends Product with Serializable

object Val {
  final case class Num(n: BigDecimal) extends Val
  final case class Str(s: String) extends Val
  case object True extends Val
  case object False extends Val
  case object Null extends Val
  case object EmptyMap extends Val
  case object EmptyList extends Val
}
