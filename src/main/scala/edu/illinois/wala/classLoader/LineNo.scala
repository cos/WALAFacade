package edu.illinois.wala.classLoader

object LineNo {
  def apply(l: Int): Option[LineNo] =
    if (l > 0)
      Some(new LineNo(l))
    else
      None
}

class LineNo private (val l: Int) extends AnyVal