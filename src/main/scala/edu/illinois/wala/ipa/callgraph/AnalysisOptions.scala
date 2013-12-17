package edu.illinois.wala.ipa.callgraph

import com.typesafe.config.ConfigFactory
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.types.TypeReference
import com.ibm.wala.types.MethodReference
import com.ibm.wala.types.TypeName
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint
import com.ibm.wala.ipa.callgraph.Entrypoint
import scala.collection.JavaConverters._
import com.typesafe.config.Config
import com.ibm.wala.classLoader.ClassLoaderFactoryImpl
import com.ibm.wala.classLoader.ClassLoaderFactory
//import com.ibm.wala.cast.java.translator.polyglot.PolyglotClassLoaderFactory
//import com.ibm.wala.cast.java.translator.polyglot.JavaIRTranslatorExtension
import com.ibm.wala.types.ClassLoaderReference

class AnalysisOptions(scope: AnalysisScope, entrypoints: java.lang.Iterable[Entrypoint], val cha: ClassHierarchy, val isSourceAnalysis: Boolean)
  extends com.ibm.wala.ipa.callgraph.AnalysisOptions(scope, entrypoints) {
}

object AnalysisOptions {
  def apply(entrypoints: Iterable[(String, String)], scope: AnalysisScope, classLoaderFactory: ClassLoaderFactory, isSourceAnalysis: Boolean) = {

    implicit val s = scope

    implicit val cha = ClassHierarchy.make(scope, classLoaderFactory)

    println(scope)

    val entrypointsW = entrypoints map { case (klass, method) => makeEntrypoint(klass, method) } asJava

    new AnalysisOptions(scope, entrypointsW, cha, isSourceAnalysis)
  }

  def apply()(implicit config: Config = ConfigFactory.load): AnalysisOptions = {
    apply((config.getString("wala.entry.class"), config.getString("wala.entry.method")), Set())
  }

  def apply(
    entrypoints: Iterable[(String, String)],
    dependencies: Iterable[Dependency])(
      implicit config: Config): AnalysisOptions = {

    val binDep = if (config.hasPath("wala.dependencies.binary"))
      config.getList("wala.dependencies.binary").asScala map { d => Dependency(d.unwrapped.asInstanceOf[String]) }
    else
      List()

    val srcDep = if (config.hasPath("wala.dependencies.source"))
      config.getList("wala.dependencies.source").asScala map { d => Dependency(d.unwrapped.asInstanceOf[String], DependencyNature.SourceDirectory) }
    else
      List()

    val jarDep = if (config.hasPath("wala.dependencies.jar"))
      config.getList("wala.dependencies.jar").asScala map { d => Dependency(d.unwrapped.asInstanceOf[String], DependencyNature.Jar) }
    else
      List()

    val dep = binDep ++ srcDep ++ jarDep ++ dependencies

    val jreLibPath = if (config.hasPath("wala.jre-lib-path"))
      config.getString("wala.jre-lib-path")
    else
      System.getenv().get("JAVA_HOME") + "/jre/lib/rt.jar"

    val scope = new AnalysisScope(jreLibPath, config.getString("wala.exclussions"), dep)

    val classLoaderImpl =
      //      if (!srcDep.isEmpty) 
      //      new PolyglotClassLoaderFactory(scope.getExclusions(), new JavaIRTranslatorExtension())
      //    else
      new ClassLoaderFactoryImpl(scope.getExclusions())

    apply(entrypoints, scope, classLoaderImpl, !srcDep.isEmpty)
  }

  def apply(klass: String, method: String)(implicit config: Config): AnalysisOptions = apply((klass, method), Seq())

  def apply(entrypoint: (String, String),
    dependencies: Iterable[Dependency])(implicit config: Config): AnalysisOptions = apply(Seq(entrypoint), dependencies)

  val mainMethod = "main([Ljava/lang/String;)V"

  def makeEntrypoint(entryClass: String, entryMethod: String)(implicit scope: AnalysisScope, cha: ClassHierarchy): Entrypoint = {
    val methodReference = AnalysisScope.allScopes.toStream
      .map { scope.getLoader(_) }
      .map { TypeReference.findOrCreate(_, TypeName.string2TypeName(entryClass)) }
      .map { MethodReference.findOrCreate(_, entryMethod.substring(0, entryMethod.indexOf('(')), entryMethod.substring(entryMethod.indexOf('('))) }
      .find { cha.resolveMethod(_) != null } getOrElse { throw new Error("Could not find entrypoint: " + entryClass + "#" + entryMethod + " anywhere in loaded classes.") }

    new DefaultEntrypoint(methodReference, cha)
  }

}