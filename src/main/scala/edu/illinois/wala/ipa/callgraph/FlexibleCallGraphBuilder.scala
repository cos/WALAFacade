package edu.illinois.wala.ipa.callgraph

import com.ibm.wala.ipa.callgraph._
import com.ibm.wala.ipa.callgraph.propagation._
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.ipa.callgraph.propagation.cfa.DefaultPointerKeyFactory
import scala.collection.JavaConverters._
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.ibm.wala.ssa.IRFactory
import com.ibm.wala.classLoader.IMethod
//import com.ibm.wala.cast.ir.ssa.AstIRFactory
import com.ibm.wala.ssa.DefaultIRFactory
//import com.ibm.wala.cast.java.ipa.callgraph.AstJavaSSAPropagationCallGraphBuilder

object FlexibleCallGraphBuilder {
  def apply(entrypoint: (String, String), dependencies: Iterable[Dependency])(implicit config: Config): AbstractCallGraphBuilder =
    apply(AnalysisOptions(Seq(entrypoint), dependencies))

  def apply(entrypoint: (String, String), dependency: String)(implicit config: Config = ConfigFactory.load): AbstractCallGraphBuilder =
    apply(entrypoint, Seq(Dependency(dependency)))
    
  def apply()(implicit config: Config): AbstractCallGraphBuilder = 
    apply(AnalysisOptions())

  def apply(options: AnalysisOptions): AbstractCallGraphBuilder = 
//    if(options.isSourceAnalysis)
//      new AstFlexibleCallGraphBuilder(options)
//    else
      new FlexibleCallGraphBuilder(options)
}

class FlexibleCallGraphBuilder(
  val _cha: ClassHierarchy,
  val _options: AnalysisOptions,
  val _cache: AnalysisCache, pointerKeys: PointerKeyFactory)
  extends SSAPropagationCallGraphBuilder(_cha, _options, _cache, pointerKeys)
  with AbstractCallGraphBuilder with ExtraFeatures {

  // Constructors
  def this(cha: ClassHierarchy, options: AnalysisOptions, irFactory: IRFactory[IMethod]) = this(cha, options, new AnalysisCache(irFactory), new DefaultPointerKeyFactory())
  def this(options: AnalysisOptions) = this(options.cha, options,
//    if (options.isSourceAnalysis)
//      AstIRFactory.makeDefaultFactory()
//    else
      new DefaultIRFactory())

  final lazy val heap = getPointerAnalysis().getHeapGraph()

  setContextInterpreter(theContextInterpreter)
  setContextSelector(cs)
  setInstanceKeys(instanceKeys)

  val cg = makeCallGraph(options)
  val cache = _cache

  // expose implicits
  implicit val implicitCha = cha
}

/// !!! code duplication
//
//class AstFlexibleCallGraphBuilder(
//  val _cha: ClassHierarchy,
//  val _options: AnalysisOptions,
//  val _cache: AnalysisCache, pointerKeys: PointerKeyFactory)
//  extends AstJavaSSAPropagationCallGraphBuilder(_cha, _options, _cache, pointerKeys)
//  with AbstractCallGraphBuilder with ExtraFeatures {
//
//  // Constructors
//  def this(cha: ClassHierarchy, options: AnalysisOptions, irFactory: IRFactory[IMethod]) = this(cha, options, new AnalysisCache(irFactory), new DefaultPointerKeyFactory())
//  def this(options: AnalysisOptions) = this(options.cha, options,
//    if (options.isSourceAnalysis)
//      AstIRFactory.makeDefaultFactory()
//    else
//      new DefaultIRFactory())
//
//  final lazy val heap = getPointerAnalysis().getHeapGraph()
//
//  setContextInterpreter(theContextInterpreter)
//  setContextSelector(cs)
//  setInstanceKeys(instanceKeys)
//
//  val cg = makeCallGraph(options)
//  val cache = _cache
//
//  // expose implicits
//  implicit val implicitCha = cha
//}