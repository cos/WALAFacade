package edu.illinois.wala.classLoader

import wala.WALAConversions._

object M {
  def unapply(m: M): Option[(C, String)] = {
    Some(m.getDeclaringClass(), m.getSelector().toString())
  }
}

class RichM(val m: M) extends AnyVal {
  def name = m.getSelector().getName().toString()

  def prettyPrint: String = {
    val packageName = m.getDeclaringClass().getName().getPackage().toString().replace('/', '.')
    packageName + "." + m.getDeclaringClass().name + "." + m.name
  }
}