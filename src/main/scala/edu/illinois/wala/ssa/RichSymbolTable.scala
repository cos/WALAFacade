package edu.illinois.wala.ssa

import com.ibm.wala.ssa.SymbolTable

case class RichSymbolTable(val st: SymbolTable) extends Iterable[V] {
  val elements = Stream.range(0, st.getMaxValueNumber) map { V(_) }

  def iterator = elements.iterator
}