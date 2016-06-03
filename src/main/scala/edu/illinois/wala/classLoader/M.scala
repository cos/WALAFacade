package edu.illinois.wala.classLoader

import com.ibm.wala.cast.loader.AstMethod
import edu.illinois.wala.Facade._
import com.ibm.wala.classLoader.ShrikeBTMethod
import edu.illinois.wala.ssa.IRNo
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode
import com.ibm.wala.classLoader.NewSiteReference

object M {
  def unapply(m: M): Option[(C, String)] = {
    Some(m.getDeclaringClass(), m.getSelector().toString())
  }
}

class RichM(val m: M) extends AnyVal {
  def name = m.getSelector().getName().toString()

  def prettyPrint: String = {
    val packageName = m.getDeclaringClass().getName().getPackage() match {
      case p: AnyRef => p.toString().replace('/', '.')
      case null => ""
    }
    packageName + "." + m.getDeclaringClass().name + "." + m.name
  }

  def c = m.getDeclaringClass

  def lineNo(i: ProgramCounter): Option[LineNo] = LineNo(m.getLineNumber(i))

  //  def lineNo[T: (ProgramCounter OR IRNo)#LAMBDA](pc: Option[T]): Option[LineNo] = pc flatMap {
  //    _ match {
  //      case pc: ProgramCounter => lineNo(pc)
  //      case i: IRNo => lineNo(i)
  //    }
  //  }

  def lineNo(i: IRNo): Option[LineNo] = m match {
    case m: ShrikeBTMethod => ProgramCounter(m.getBytecodeIndex(i)) flatMap { lineNo(_) }
    case m: AstMethod => ProgramCounter(m.getLineNumber(i)) flatMap { lineNo(_) }
    case _ => None
  }
  
  def lineNo(site: NewSiteReference) = ProgramCounter(site.getProgramCounter())
}