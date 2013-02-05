package wala

import com.ibm.wala.classLoader.IMethod
import scala.collection.JavaConversions._
import com.ibm.wala.ipa.callgraph.Context
import com.ibm.wala.ipa.callgraph.Context
import com.ibm.wala.classLoader.IClass
import com.ibm.wala.classLoader.ShrikeBTMethod
import com.ibm.wala.util.intset.IntSet
import com.ibm.wala.util.intset.IntSetAction
import com.ibm.wala.types.TypeReference
import com.ibm.wala.types.ClassLoaderReference
import com.ibm.wala.ipa.callgraph.ContextKey
import com.ibm.wala.ipa.callgraph.DelegatingContext
import com.ibm.wala.util.Predicate
import scala.collection._
import com.ibm.wala.util.intset.SparseIntSet
import com.ibm.wala.ipa.slicer.Statement
import com.ibm.wala.ipa.slicer.StatementWithInstructionIndex
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode
import com.ibm.wala.util.collections.EmptyIterator
import com.ibm.wala.ipa.callgraph.CallGraph
import com.ibm.wala.classLoader.NewSiteReference
import edu.illinois.wala.ipa.callgraph.propagation.WrapO
import edu.illinois.wala.S
import edu.illinois.wala.Named
import edu.illinois.wala._
import edu.illinois.wala.ssa.V
import edu.illinois.wala.TypeAliases

class WALAConversions extends TypeAliases
  with edu.illinois.wala.util.Wrapper
  with ipa.slicer.Wrapper
  with classLoader.Wrapper
  with types.Wrapper
  with ssa.Wrapper
  with ipa.callgraph.Wrapper
  with Wrapper {

  type NOT[A] = A => Nothing
  type INNEROR[T, U] = NOT[NOT[T] with NOT[U]]
  type NOTNOT[A] = NOT[NOT[A]]
  type OR[T, U] = { type LAMBDA[X] = NOTNOT[X] <:< (T INNEROR U) }

  //  def printCodeLocation(n: N, bytecodeIndex: Int): String = {
  //    printCodeLocation(n.getMethod(), bytecodeIndex)
  //  }

  implicit def mWithLineNo(m: M) = new {
    def lineNoFromBytecodeIndex(bytecodeIndex: Int) = m match {
      case m: ShrikeBTMethod => m.getLineNumber(bytecodeIndex)
      case _ => -1
    }
    def lineNoFromIRNo(irNo: Int) = lineNoFromBytecodeIndex(m.asInstanceOf[ShrikeBTMethod].getBytecodeIndex(irNo))
  }

  //  	public static String variableName(Integer v, CGNode cgNode,
  //			int ssaInstructionNo) {
  //		String[] localNames;
  //		try {
  //			localNames = cgNode.getIR().getLocalNames(ssaInstructionNo,
  //					v);
  //		} catch (Exception e) {
  //			localNames = null;
  //		} catch (UnimplementedError e) {
  //			localNames = null;
  //		} 
  //		String variableName = null;
  //		if (localNames != null && localNames.length > 0)
  //			variableName = localNames[0];
  //		return variableName;
  //	}

  // access instructions of Some(object, field, is_write)
  //  object AccessI {
  //    def unapply(i: I):Option[(F, V)] = {
  //      i match {
  //        case rI: SSAGetInstruction => Some(rI.getDeclaredField(), rI.getUse(0))
  //        case wI: SSAPutInstruction => Some(wI.getDeclaredField(), wI.getDef())
  //        case _ => None
  //      }
  //    }
  //  }

  val mainMethod = "main([Ljava/lang/String;)V";
}

object WALAConversions extends WALAConversions
object WC extends WALAConversions