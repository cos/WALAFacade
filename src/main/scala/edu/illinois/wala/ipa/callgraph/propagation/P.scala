package edu.illinois.wala.ipa.callgraph.propagation
import com.ibm.wala.ssa.SSAPutInstruction
import scala.collection.JavaConversions._
import com.ibm.wala.ipa.callgraph.propagation.InstanceFieldKey
import com.ibm.wala.ipa.callgraph.propagation.ArrayContentsKey
import com.ibm.wala.ipa.modref.ArrayLengthKey
import com.ibm.wala.ipa.callgraph.propagation.PointerKey
import com.ibm.wala.ipa.callgraph.propagation.AbstractFieldPointerKey
import edu.illinois.wala.S
import edu.illinois.wala.ssa.V
import wala.WALAConversions._

object P {
  def apply(n: N, v: Int) = {
    new LocalP(n, v)
  }
  def unapply(p: LocalP): Option[(N, Int)] = {
    Some((p.n, p.v))
  }
}

trait WrapP {
  implicit def wrapLocalP(p: LocalP): RichLocalP = new RichLocalP(p)
  implicit def enhancePK(p: P) = new RichP(p)
}

class RichLocalP(val p: LocalP) extends AnyVal {
  def n = p.getNode()
  def v = V(p.getValueNumber())
  def defI = n.getDU().getDef(v)
  def uses: Iterable[I] = n.getDU().getUses(v).toIterable

  /**
   * Gets all uses of this pointer that write to a field of it
   */
  def puts: Iterable[PutI] =
    (for (i <- uses if i.isInstanceOf[PutI] && V(i.asInstanceOf[PutI].getRef()) == v)
      yield i.asInstanceOf[SSAPutInstruction])

  def variableNames(): Iterable[String] =
    n.instructions.toIterable.map(i => S(n, i).variableNames(v)).flatten.toSet

  def prettyPrint: String =
    n.prettyPrint + " v" + v + "(" + (if (!variableNames().isEmpty) variableNames.reduce(_ + "," + _) else "") + ")"
}

class RichP(val p: P) extends AnyVal {
  def prettyPrint(): String = prettyPrint(".")

  def prettyPrint(sep: String): String = p match {
    case p: LocalP => p.prettyPrint
    case p: AbstractFieldPointerKey => "IFK:" + p.getInstanceKey().prettyPrint + (p match {
      case p: InstanceFieldKey => sep + p.getField().prettyPrint
      case p: ArrayContentsKey => sep + "[]"
      case p: ArrayLengthKey => sep + "ARR_LENGTH"
    })
    case _ => p.toString
  }
}
