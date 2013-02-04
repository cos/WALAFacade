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

class WALAConversions extends TypeAliases
  with WALAConversionsForP
  with WrapO
  with edu.illinois.wala.util.Wrapper
  with ipa.slicer.Wrapper
  with classLoader.Wrapper
  with types.Wrapper
  with ssa.Wrapper 
  with ipa.callgraph.Wrapper {

  def printCodeLocation(n: N, bytecodeIndex: Int): String = {
    printCodeLocation(n.getMethod(), bytecodeIndex)
  }

  implicit def mWithLineNo(m: M) = new {
    def lineNoFromBytecodeIndex(bytecodeIndex: Int) = m match {
      case m: ShrikeBTMethod => m.getLineNumber(bytecodeIndex)
      case _ => -1
    }
    def lineNoFromIRNo(irNo: Int) = lineNoFromBytecodeIndex(m.asInstanceOf[ShrikeBTMethod].getBytecodeIndex(irNo))
  }

  def printCodeLocation(m: IMethod, bytecodeIndex: Int): String = {
    val lineNo = m.lineNoFromBytecodeIndex(bytecodeIndex)
    val className = m.getDeclaringClass().getName().getClassName().toString()
    "" + m.prettyPrint + "(" + className.split("\\$")(0) + ".java:" + lineNo + ")"
  }

  def inApplicationScope(n: N): Boolean = inApplicationScope(n.m)
  def inApplicationScope(m: M): Boolean = inApplicationScope(m.getDeclaringClass)
  def inApplicationScope(c: C): Boolean = c.getClassLoader().getReference() == ClassLoaderReference.Application

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

  implicit def statementHasN(s: Statement) = new {
    def n = s.getNode
  }


  val mainMethod = "main([Ljava/lang/String;)V";
}

object WALAConversions extends WALAConversions
object WC extends WALAConversions