package edu.illinois.wala.classLoader

import wala.WALAConversions._

object F {

}

class RichF(val f: F) extends AnyVal {
  def prettyPrint: String = f.getName().toString()
}