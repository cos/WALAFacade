package edu.illinois.wala.ssa

import com.ibm.wala.ssa.IR
import com.ibm.wala.ssa.DefUse

object V {
  implicit object theOrdering extends Ordering[V] {
    def compare(v1: V, v2: V) = v1.v - v2.v
  }
}

case class V(val v: Int) extends AnyVal {
  override def toString() = "v" + v

  def names(implicit ir: IR) = {
    (Range(0, ir.getInstructions().length - 1) map { ir.getLocalNames(_, v) } filter (_ != null) flatten) filter (_ != null)
  }
  def name(implicit ir: IR) = names headOption
  
  def getDef(implicit du: DefUse) = du.getDef(v)
}