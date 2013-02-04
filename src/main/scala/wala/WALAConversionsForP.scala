package wala
import com.ibm.wala.ssa.SSAPutInstruction
import scala.collection.JavaConversions._
import com.ibm.wala.ipa.callgraph.propagation.InstanceFieldPointerKey
import com.ibm.wala.ipa.callgraph.propagation.InstanceFieldKey
import com.ibm.wala.ipa.callgraph.propagation.ArrayContentsKey
import com.ibm.wala.ipa.modref.ArrayLengthKey
import com.ibm.wala.ipa.callgraph.propagation.PointerKey
import com.ibm.wala.ipa.callgraph.propagation.AbstractFieldPointerKey

// "Pointer Local" - equivalent of LocalPointerKey
trait WALAConversionsForP { self: WALAConversions =>

  object P {
    def apply(n: N, v: Int) = {
      new P(n, v)
    }
    def unapply(p: P): Option[(N, Int)] = {
      Some((p.getNode(), p.getValueNumber()))
    }
  }

  class EnhancedP(p: P) {
    def n = p.getNode()
    def v = p.getValueNumber()
    def getDef() = n.getDU().getDef(v)
    def getUses(): Iterable[I] = n.getDU().getUses(v).toIterable

    /**
     * Gets all uses of this pointer that write to a field of it
     */
    def getPuts(): Iterable[SSAPutInstruction] =
      (for (i <- getUses() if i.isInstanceOf[PutI] && i.asInstanceOf[PutI].getRef() == v)
        yield i.asInstanceOf[SSAPutInstruction])

    def variableNames(): Iterable[String] =
      n.instructions.toIterable.map(i => S(n, i).variableNames(v)).flatten.toSet

    def prettyPrint(): String =
      n.prettyPrint + " v" + v + "(" + (if (!variableNames().isEmpty) variableNames.reduce(_ + "," + _) else "") + ")"
  }

  implicit def enhanceP(p: P): EnhancedP = new EnhancedP(p)

  implicit def enhancePK(p: PointerKey) = new {
    def prettyPrint(): String = prettyPrint(".")

    def prettyPrint(sep: String): String = p match {
      case p: P => p.prettyPrint
      case p: AbstractFieldPointerKey => "IFK:" + p.getInstanceKey().prettyPrint + (p match {
        case p: InstanceFieldKey => sep + p.getField().prettyPrint
        case p: ArrayContentsKey => sep + "[]"
        case p: ArrayLengthKey => sep + "ARR_LENGTH"
      })
      case _ => p.toString
    }
  }
}