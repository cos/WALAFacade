package edu.illinois.wala.ssa

import wala.WALAConversions._
import com.ibm.wala.ipa.callgraph.propagation.PointerAnalysis
import com.ibm.wala.shrikeBT.analysis.ClassHierarchy
import com.ibm.wala.ipa.cha.IClassHierarchy

class RichPutI(val i:PutI) extends AnyVal {
  def v = V(i.getVal())
}

class RichGetI(val i:GetI) extends AnyVal {
  def d = V(i.getDef())
}

class RichInvokeI(val i:InvokeI) extends AnyVal {
  def m(implicit cha: IClassHierarchy) = cha.resolveMethod(i.getDeclaredTarget())
}

class RichI(val i: I) extends AnyVal {
  def uses: Stream[V] = Stream.range(0, i.getNumberOfUses()).map(index => { V(i.getUse(index)) })
}