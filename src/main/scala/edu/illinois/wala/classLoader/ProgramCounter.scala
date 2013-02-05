package edu.illinois.wala.classLoader

import edu.illinois.wala.Facade._

object ProgramCounter {
  def apply(i: Int): Option[ProgramCounter] = if (i > 0)
    Some(new ProgramCounter(i))
  else
    None
}