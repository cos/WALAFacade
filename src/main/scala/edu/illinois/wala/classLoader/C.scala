package edu.illinois.wala.classLoader

import wala.WALAConversions._
import edu.illinois.wala.Named
import edu.illinois.wala.PrettyPrintable

object C {
  def unapply(c: C): Option[(String, String)] = {
    Some(c.getName().getPackage().toString(), c.getName().getClassName().toString())
  }
}

class RichC(val c: C) extends AnyVal with Named with PrettyPrintable {
  def name = c.getName().getClassName().toString()

  def prettyPrint: String = c.getReference().prettyPrint

  def sourceFilePath = c.getName.toString.substring(1).split("\\$")(0)
}