package edu.illinois.wala.classLoader

import wala.WALAConversions._
import com.ibm.wala.classLoader.ShrikeBTMethod
import edu.illinois.wala.ssa.IRNo

object CodeLocation {
  def apply(m: M, i: IRNo): Option[CodeLocation] = m match {
    case m: ShrikeBTMethod => ProgramCounter(m.getBytecodeIndex(i)) map { new CodeLocation(m, _) }
    case _ => None
  }
  def apply(m: M, pc: Option[ProgramCounter]): Option[CodeLocation] = pc map { new CodeLocation(m, _)}
}

case class CodeLocation(m: M, bytecodeIndex: ProgramCounter) {
  lazy val lineNo = m.getLineNumber(bytecodeIndex)

  override def toString = {
    val lineNo = m.lineNo(bytecodeIndex)
    val className = m.getDeclaringClass().getName().getClassName().toString()
    "" + m.prettyPrint + "(" + className.split("\\$")(0) + ".java:" + lineNo + ")"
  }
}