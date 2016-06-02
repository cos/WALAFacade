package edu.illinois.wala.classLoader

import com.ibm.wala.cast.loader.AstMethod
import edu.illinois.wala.Facade._
import com.ibm.wala.classLoader.ShrikeBTMethod
import edu.illinois.wala.ssa.IRNo

object CodeLocation {
  def apply(m: M, i: IRNo): Option[CodeLocation] = m match {
    case m: ShrikeBTMethod => ProgramCounter(m.getBytecodeIndex(i)) map { new CodeLocation(m, _) }
    case m: AstMethod => ProgramCounter(i) map { new CodeLocation(m, _)}
    case _ => None
  }
}

case class CodeLocation(m: M, bytecodeIndex: ProgramCounter) {
  lazy val lineNo = m.lineNo(bytecodeIndex)
  
  override def toString = {
    val className = m.getDeclaringClass().getName().getClassName().toString()
    val s = "" + m.prettyPrint + "(" + className.split("\\$")(0) + ".java:" + (lineNo getOrElse "") + ")"
    s
  }
}