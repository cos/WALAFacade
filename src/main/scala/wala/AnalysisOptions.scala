package wala

import com.typesafe.config.ConfigFactory
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.types.TypeReference
import com.ibm.wala.types.MethodReference
import com.ibm.wala.types.TypeName
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint
import com.ibm.wala.ipa.callgraph.Entrypoint
import scala.collection.JavaConverters._
import wala.AnalysisScope._
import com.typesafe.config.Config

class AnalysisOptions(scope: AnalysisScope, entrypoints: java.lang.Iterable[Entrypoint], val cha: ClassHierarchy)
  extends com.ibm.wala.ipa.callgraph.AnalysisOptions(scope, entrypoints)

object AnalysisOptions {
  def apply(entrypoints: Iterable[(String, String)], scope: AnalysisScope) = {

    implicit val s = scope
    implicit val cha = ClassHierarchy.make(scope)

    println(scope)

    val entrypointsW = entrypoints map { case (klass, method) => makeEntrypoint(klass, method) } asJava

    new AnalysisOptions(scope, entrypointsW, cha)
  }

  def apply()(implicit config: Config = ConfigFactory.load): AnalysisOptions = {
    apply((config.getString("wala.entry.class"), config.getString("wala.entry.method")), Set())
  }

  def apply(
    entrypoints: Iterable[(String, String)],
    dependencies: Iterable[Dependency])(
      implicit config: Config): AnalysisOptions = {

    val binDep = config.getList("wala.dependencies.binary").asScala map { d => Dependency(d.unwrapped.asInstanceOf[String]) }
    val jarDep = config.getList("wala.dependencies.jar").asScala map { d => Dependency(d.unwrapped.asInstanceOf[String], DependencyNature.Jar) }
    val dep = binDep ++ jarDep ++ dependencies

    val scope = new AnalysisScope(config.getString("wala.jre-lib-path"), config.getString("wala.exclussions-file"), dep)
    apply(entrypoints, scope)
  }

  def apply(klass: String, method: String)(implicit config: Config): AnalysisOptions = apply((klass, method), Seq())

  def apply(entrypoint: (String, String),
    dependencies: Iterable[Dependency])(implicit config: Config): AnalysisOptions = apply(Seq(entrypoint), dependencies)

  val mainMethod = "main([Ljava/lang/String;)V"

  def makeEntrypoint(entryClass: String, entryMethod: String)(implicit scope: AnalysisScope, cha: ClassHierarchy): Entrypoint = {
    val typeReference: TypeReference = TypeReference.findOrCreate(scope.getLoader(AnalysisScope.Application),
      TypeName.string2TypeName(entryClass))
    val methodReference: MethodReference = MethodReference.findOrCreate(typeReference,
      entryMethod.substring(0, entryMethod.indexOf('(')), entryMethod.substring(entryMethod.indexOf('(')))
    new DefaultEntrypoint(methodReference, cha)
  }
}