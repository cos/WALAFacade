package edu.illinois.wala.ipa.callgraph.propagation

import wala.WALAConversions._
import com.ibm.wala.util.collections.EmptyIterator

object unknownO extends O {
  override def getConcreteType() = null
  override def getCreationSites(cg: G) = EmptyIterator.instance()

  override def toString = "UNKOWN object"
}