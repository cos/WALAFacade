package edu.illinois.wala.ssa

object V {
  def apply(v: Int):V = new V(v)
}

class V(val v: Int) extends AnyVal {

}