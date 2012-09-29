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

class WALAConversions extends TypeAliases with WALAConversionsForN with WALAConversionsForP {
  trait Named {
    def name(): String
  }

  implicit def m2named(m: M): Named = new Named {
    def name = m.getSelector().getName().toString()
  }

  implicit def c2named(c: C): Named = new Named {
    def name = c.getName().getClassName().toString()
  }

  trait PrettyPrintable {
    def prettyPrint(): String
  }

  implicit def m2prettyprintable(m: IMethod): PrettyPrintable = new PrettyPrintable {
    def prettyPrint(): String = {
      val packageName = m.getDeclaringClass().getName().getPackage().toString().replace('/', '.')
      packageName + "." + m.getDeclaringClass().name + "." + m.name
    }
  }

  implicit def type2prettyprintable(t: TypeReference): PrettyPrintable = new PrettyPrintable {
    def prettyPrint(): String = {
      val packageName = t.getName().getPackage().toString().replace('/', '.')
      packageName + "." + t.getName().getClassName()
    }
  }

  implicit def c2prettyprintable(t: C): PrettyPrintable = new PrettyPrintable {
    def prettyPrint(): String =
      try {
        val packageName = t.getName().getPackage().toString().replace('/', '.')
        packageName + "." + t.getName().getClassName()
      } catch {
        case e: NullPointerException => "null"
      }
  }

  // method
  object M {
    def unapply(m: M): Option[(IClass, String)] = {
      Some(m.getDeclaringClass(), m.getSelector().toString())
    }
  }

  // class, iclass
  object C {
    def unapply(c: C): Option[(String, String)] = {
      Some(c.getName().getPackage().toString(), c.getName().getClassName().toString())
    }
  }

  def codeLocation(n: N, bytecodeIndex: Int): String = {
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
    "" + m.prettyPrint + "(" + className + ".java:" + lineNo + ")"
  }

  implicit def iWithEasyUses(i: I) = new {
    def uses: Iterable[V] = Stream.range(0, i.getNumberOfUses()).map(index => { i.getUse(index) })
  }

  implicit def o2prettyprintable(o: O): PrettyPrintable = new PrettyPrintable {
    def prettyPrint(): String = O.prettyPrint(o)
  }

  implicit def f2prettyprintable(f: F): PrettyPrintable = new PrettyPrintable {
    def prettyPrint(): String = f.getName().toString()
  }

  implicit def intsetTraversable(s: IntSet) = new Traversable[Int] {
    def foreach[U](f: Int => U) = {
      s.foreach(new IntSetAction() {
        override def act(x: Int) = f(x)
      })
    }
  }

  def inApplicationScope(n: N): Boolean = inApplicationScope(n.m)
  def inApplicationScope(m: M): Boolean = {
    val classLoader = m.getDeclaringClass().getClassLoader();
    classLoader.getReference() == ClassLoaderReference.Application
  }

  def inPrimordialScope(n: N): Boolean = inPrimordialScope(n.m)
  def inPrimordialScope(m: M) = {
    val classLoader = m.getDeclaringClass().getClassLoader();
    classLoader.getReference() == ClassLoaderReference.Primordial
  }

  object unknownO extends O {
    override def getConcreteType() = null
    override def toString = "UNKOWN object"
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
  
  implicit def contextWithIs(c: Context) = new {
    def is(k:ContextKey) = c.get(k) != null
  }
  
  implicit def contextWithAdd(c: Context) = new {
    def +(addedC:Context):Context = new DelegatingContext(c, addedC)
  }

  val mainMethod = "main([Ljava/lang/String;)V";
}

object WALAConversions extends WALAConversions
object WC extends WALAConversions