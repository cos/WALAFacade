package edu.illinois.wala.ssa

import com.ibm.wala.ssa.SymbolTable
import wala.WALAConversions._

trait Wrapper {
	implicit def wrapSymbolTable(st: SymbolTable) = new RichSymbolTable(st)
	
	implicit def wrapI(i: I) = new RichI(i)
	implicit def wrapI(i: PutI) = new RichPutI(i)
	implicit def wrapI(i: GetI) = new RichGetI(i)
	
	implicit def unwrapV(v: V) = v.v 
}