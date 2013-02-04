package edu.illinois.wala.ipa.callgraph

import com.ibm.wala.ipa.callgraph.Context
import wala.WALAConversions._

trait Wrapper extends propagation.WrapO with propagation.WrapP {
  implicit def wrapContext(c: Context) = new RichContext(c)
  implicit def wrapN(n: N) = new RichN(n)
}