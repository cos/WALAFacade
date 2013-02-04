package edu.illinois.wala.classLoader

import wala.WALAConversions._

trait Wrapper {
	implicit def wrapC(c: C) = new RichC(c)
	implicit def wrapM(m: M) = new RichM(m)
	implicit def wrapF(f: F) = new RichF(f)
}