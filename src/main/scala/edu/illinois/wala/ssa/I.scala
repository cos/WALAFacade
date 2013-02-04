package edu.illinois.wala.ssa

import wala.WALAConversions._

class RichPutI(val i:PutI) extends AnyVal {
  def v = V(i.getVal())
}

class RichGetI(val i:GetI) extends AnyVal {
  def d = V(i.getDef())
}

class RichI(val i: I) extends AnyVal {
  def uses: Stream[V] = Stream.range(0, i.getNumberOfUses()).map(index => { V(i.getUse(index)) })
}