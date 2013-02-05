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

  // Union Types - see http://www.chuusai.com/2011/06/09/scala-union-types-curry-howard/ 
  type NOT[A] = A => Nothing
  type INNEROR[T, U] = NOT[NOT[T] with NOT[U]]
  type NOTNOT[A] = NOT[NOT[A]]
  type OR[T, U] = { type LAMBDA[X] = NOTNOT[X] <:< (T INNEROR U) }

  val mainMethod = "main([Ljava/lang/String;)V";
}

object WALAConversions extends WALAConversions
object WC extends WALAConversions