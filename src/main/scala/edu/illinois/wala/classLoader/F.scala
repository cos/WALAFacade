package edu.illinois.wala.classLoader

import edu.illinois.wala.Facade._

object F {

}

class RichF(val f: F) extends AnyVal {
  def prettyPrint: String = f.getName().toString()
}