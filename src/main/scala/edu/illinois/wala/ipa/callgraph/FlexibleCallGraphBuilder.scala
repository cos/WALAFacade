package edu.illinois.wala.ipa.callgraph

import com.ibm.wala.ipa.callgraph._
import com.ibm.wala.ipa.callgraph.propagation._
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.ipa.callgraph.propagation.cfa.DefaultPointerKeyFactory
import scala.collection.JavaConverters._
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

object FlexibleCallGraphBuilder {
  def apply(entrypoint: (String, String), dependencies: Iterable[Dependency])(implicit config: Config) = new FlexibleCallGraphBuilder(AnalysisOptions(Seq(entrypoint), dependencies))
  def apply(entrypoint: (String, String), dependency: String)(implicit config: Config = ConfigFactory.load): FlexibleCallGraphBuilder = apply(entrypoint, Seq(Dependency(dependency)))
  def apply()(implicit config: Config) = new FlexibleCallGraphBuilder(AnalysisOptions())
}

class FlexibleCallGraphBuilder(
  val _cha: ClassHierarchy,
  val _options: AnalysisOptions,
  val _cache: AnalysisCache, pointerKeys: PointerKeyFactory)
  extends SSAPropagationCallGraphBuilder(_cha, _options, _cache, pointerKeys)
  with AbstractCallGraphBuilder with ExtraFeatures {

  // Constructors
  def this(cha: ClassHierarchy, options: AnalysisOptions) = this(cha, options, new AnalysisCache, new DefaultPointerKeyFactory())
  def this(options: AnalysisOptions) = this(options.cha, options)

  final lazy val heap = getPointerAnalysis().getHeapGraph()

  setContextInterpreter(theContextInterpreter)
  setContextSelector(cs)
  setInstanceKeys(instanceKeys)

  val cg = makeCallGraph(options)
  val cache = _cache
}