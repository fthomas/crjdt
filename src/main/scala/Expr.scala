import Expr._

sealed trait Expr extends Product with Serializable {
  // syntax

  def downField(key: String): Expr =
    DownField(this, key)
  
  def iter: Expr =
    Iter(this)
    
  def next: Expr =
    Next(this)
    
  def keys: Expr =
    Keys(this)
  
  def values: Expr =
    Values(this)
}

object Expr {
  case object Doc extends Expr
  case class Var(name: String) extends Expr
  case class DownField(expr: Expr, key: String) extends Expr
  case class Iter(expr: Expr) extends Expr
  case class Next(expr: Expr) extends Expr
  case class Keys(expr: Expr) extends Expr
  case class Values(expr: Expr) extends Expr
}
