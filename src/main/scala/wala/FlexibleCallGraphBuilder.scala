package wala

import com.ibm.wala.ipa.callgraph._
import com.ibm.wala.ipa.callgraph.propagation._
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.ipa.callgraph.propagation.cfa.ZeroXInstanceKeys
import com.ibm.wala.ipa.callgraph.propagation.cfa.DefaultPointerKeyFactory
import com.ibm.wala.ipa.callgraph.propagation.cfa.DefaultSSAInterpreter
import com.ibm.wala.ipa.callgraph.propagation.cfa.DelegatingSSAContextInterpreter
import com.ibm.wala.analysis.reflection.ReflectionContextInterpreter
import com.ibm.wala.ipa.callgraph.impl.Util
import com.ibm.wala.types.TypeReference
import com.ibm.wala.types.MethodReference
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint
import com.ibm.wala.types.TypeName
import scala.collection.JavaConverters._
import com.ibm.wala.ipa.callgraph.impl.DefaultContextSelector
import com.ibm.wala.util.strings.Atom
import com.typesafe.config.ConfigFactory
import wala.AnalysisScope.Dependency

object FlexibleCallGraphBuilder {
  def apply(entrypoint: (String, String), dependencies: Iterable[Dependency]) = new FlexibleCallGraphBuilder(AnalysisOptions(Seq(entrypoint), dependencies))
  def apply(entrypoint: (String, String), dependency: String): FlexibleCallGraphBuilder = apply(entrypoint, Seq(Dependency(dependency)))
}

class FlexibleCallGraphBuilder(cha: ClassHierarchy, val _options: AnalysisOptions, val cache: AnalysisCache, pointerKeys: PointerKeyFactory)
  extends SSAPropagationCallGraphBuilder(cha, _options, cache, pointerKeys) with ExtraFeatures {

  import FlexibleCallGraphBuilder._

  // Constructors
  def this(cha: ClassHierarchy, options: AnalysisOptions) = this(cha, options, new AnalysisCache, new DefaultPointerKeyFactory())
  def this(options: AnalysisOptions) = this(options.cha, options)

  // just helpers
  lazy val defaultInterpreter = new DefaultSSAInterpreter(options, cache)
  lazy val reflectionInterpreter = new DelegatingSSAContextInterpreter(
    ReflectionContextInterpreter.createReflectionContextInterpreter(cha, options, cache), defaultInterpreter)
  Util.addDefaultSelectors(options, cha)
  Util.addDefaultBypassLogic(options, options.getAnalysisScope(), classOf[Util].getClassLoader(), cha)

  // Hooks
  protected def cs: ContextSelector = new DefaultContextSelector(options, cha)
  def policy = { import ZeroXInstanceKeys._; SMUSH_STRINGS | ALLOCATIONS | SMUSH_THROWABLES }
  protected def contextInterpreter = new DelegatingSSAContextInterpreter(defaultInterpreter, reflectionInterpreter)
  protected def instanceKeys = new ZeroXInstanceKeys(options, cha, theContextInterpreter, policy)

  val theContextInterpreter = contextInterpreter

  makeCallGraph(options)
  final lazy val heap = getPointerAnalysis().getHeapGraph()

  setContextSelector(cs)
  setContextInterpreter(theContextInterpreter)
  setInstanceKeys(instanceKeyFactory)
}





