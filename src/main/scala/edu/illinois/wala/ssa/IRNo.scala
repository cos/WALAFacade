package edu.illinois.wala.ssa

/**
 *  An index into the IR instruction array
 */

object IRNo {
  def apply(i: Int): Option[IRNo] = if (i >= 0)
    new Some(new IRNo(i))
  else
    None
}

class IRNo(val i: Int) extends AnyVal