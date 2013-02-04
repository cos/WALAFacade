package edu.illinois.wala.ssa

import com.ibm.wala.ssa.SymbolTable

case class RichSymbolTable(val st: SymbolTable) extends Iterable[V] {
  val elements = Stream.range(1, st.getMaxValueNumber + 1) map { V(_) }

  def iterator = elements.iterator
}