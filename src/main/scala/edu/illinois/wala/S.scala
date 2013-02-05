package edu.illinois.wala
import com.ibm.wala.ipa.cfg.BasicBlockInContext
import edu.illinois.wala.Facade._
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock
import com.ibm.wala.classLoader.ShrikeBTMethod
import sppa.util.debug
import edu.illinois.wala.ssa.V
import edu.illinois.wala.ipa.callgraph.propagation.P
import edu.illinois.wala.classLoader.CodeLocation
import edu.illinois.wala.ssa.IRNo

import Facade._

object S {
  def unapply(b: BasicBlockInContext[IExplodedBasicBlock]): Option[(N, I)] = {
    Some((b.getNode(), b.getDelegate().getInstruction()))
  }
  def unapply(s: S[I]): Option[(N, I)] = {
    Some((s.n, s.i))
  }
  def apply(n: N, i: I) = new S(n, i)

  val accessesRepo = collection.mutable.Map[S[I], Int]()
}

class S[+J <: I](val n: N, val i: J) extends PrettyPrintable {

  def prettyPrint(): String = printCodeLocation() +
    (if (debug.detailContexts) " [ " + S.accessesRepo.getOrElseUpdate(this, S.accessesRepo.size) + " ] " + " --- " + n.getContext() else "")

  def printCodeLocation(): String = irNo match {
    case Some(irNo) => CodeLocation(m, irNo).toString
    case _ => {
      val index = n.instructions collect { case i if i != null => i.toString } indexWhere { _ == i.toString }
      "IRNo-1 " + index + " ---- " + i
    }
  }

  lazy val m = n.m

  lazy val sourceFilePath = m.getDeclaringClass().sourceFilePath

  lazy val lineNo = m.lineNo(irNo)

  lazy val irNo: Option[IRNo] = IRNo(n.getIR().getInstructions().indexOf(ii => i.equals(ii)))

  def valuesForVariableName(name: String): Iterable[V] = {
    n.getIR().getSymbolTable().filter(v => irNo match {
      case Some(irNo) => {
        val names = n.getIR().getLocalNames(irNo, v)
        if (names != null) {
          names.contains(name)
        } else
          false
      }
      case None => false
    })
  }

  lazy val isStatic: Boolean = i match {
    case i: AccessI => i.isStatic
    case i: InvokeI => i.isStatic
    case _ => false
  }

  def variableNames(v: V): Iterable[String] = irNo match {
    case Some(irNo) => {
      val names = n.getIR().getLocalNames(irNo, v)
      if (names != null) names.filter(_ != null) else Iterable()
    }
    case None => Iterable[String]()
  }

  override def toString = "S(" + n + "," + i + ")"

  override def equals(other: Any) = other match {
    case that: S[_] => this.n == that.n && this.i == that.i
    case _ => false
  }

  override def hashCode = n.hashCode * 41 + i.hashCode

  lazy val refP: Option[P] = i match {
    case i: AccessI if !i.isStatic => Some(P(n, i.getRef()))
    case i: ArrayReferenceI => Some(P(n, i.getArrayRef()))
    case i: InvokeI if !i.isStatic => Some(P(n, i.getReceiver()))
    case _ => None
  }
}