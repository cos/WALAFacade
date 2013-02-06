package edu.illinois.wala.classLoader

import edu.illinois.wala.Facade._

object ProgramCounter {
  def apply(i: Int): Option[ProgramCounter] = try {
    Some(new ProgramCounter(i))
  } catch {
    case e: IllegalArgumentException => None
  }
}