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
import wala.WALAConversions.WrappedIntSet
import com.ibm.wala.ipa.slicer.StatementWithInstructionIndex
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode
import com.ibm.wala.util.collections.EmptyIterator
import com.ibm.wala.ipa.callgraph.CallGraph
import com.ibm.wala.classLoader.NewSiteReference
import edu.illinois.wala.ipa.callgraph.propagation.WrapO

class WALAConversions extends TypeAliases with WALAConversionsForN with WALAConversionsForP with WrapO {
  trait Named {
    def name(): String
  }

  implicit def makePredicateFromFunction[T](f: Function1[T, Boolean]) = new Predicate[T] {
    def test(t: T) = f(t)
  }

  implicit def makeIntSetActionFromFunction(f: Function1[Int, Unit]) = new IntSetAction {
    def act(t: Int) = f(t)
  }

  implicit def makeSFromStatement(s: StatementWithInstructionIndex) = S(s.getNode(), s.getInstruction())

  implicit def m2named(m: M): Named = new Named {
    def name = m.getSelector().getName().toString()
  }

  implicit def c2named(c: C): Named = new Named {
    def name = c.getName().getClassName().toString()
  }

  trait PrettyPrintable extends Any {
    def prettyPrint: String
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

  implicit def cWithSourceFilePath(c: C) = new {
    def sourceFilePath = Option(c) map { _.getName.toString.substring(1).split("\\$")(0) } getOrElse ""
  }

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

  implicit def iWithEasyUses(i: I) = new {
    def uses: Iterable[V] = Stream.range(0, i.getNumberOfUses()).map(index => { i.getUse(index) })
  }

  implicit def f2prettyprintable(f: F): PrettyPrintable = new PrettyPrintable {
    def prettyPrint(): String = f.getName().toString()
  }

  class WrappedIntSet(s: IntSet) extends Set[Int] {
    def contains(key: Int) = s.contains(key)
    def iterator: Iterator[Int] = {
      val it = s.intIterator()
      new Iterator[Int] {
        def hasNext = it.hasNext()
        def next = it.next()
      }
    }
    def +(elem: Int) = new WrappedIntSet(s.union(SparseIntSet.singleton(elem)))
    def -(elem: Int) = throw new Exception("unsupported, implement this if you need it")

    def |(other: IntSet) = s.union(other)
    def &(other: IntSet) = s.intersection(other)
    def intersects(other: IntSet) = s.containsAny(other)

    override def foreach[U](f: Int => U) = {
      s.foreach(new IntSetAction() {
        override def act(x: Int) = f(x)
      })
    }
  }

  implicit def intsetSet(s: IntSet) = new WrappedIntSet(s)

  def inApplicationScope(n: N): Boolean = inApplicationScope(n.m)
  def inApplicationScope(m: M): Boolean = inApplicationScope(m.getDeclaringClass)
  def inApplicationScope(c: C): Boolean = c.getClassLoader().getReference() == ClassLoaderReference.Application

  object unknownO extends O {
    override def getConcreteType() = null
    override def getCreationSites(cg: CallGraph) = EmptyIterator.instance()
    
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

  implicit def statementHasN(s: Statement) = new {
    def n = s.getNode
  }

  implicit def contextWithIs(c: Context) = new {
    def is(k: ContextKey) = c.get(k) != null
  }

  implicit def contextWithAdd(c: Context) = new {
    def +(addedC: Context): Context = new DelegatingContext(c, addedC)
  }

  val mainMethod = "main([Ljava/lang/String;)V";
}

object WALAConversions extends WALAConversions
object WC extends WALAConversions