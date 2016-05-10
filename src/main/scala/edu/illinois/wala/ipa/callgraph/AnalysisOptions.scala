package edu.illinois.wala.ipa.callgraph

import com.ibm.wala.cast.java.translator.jdt.JDTClassLoaderFactory
import com.ibm.wala.cast.java.translator.jdt.ejc.EJCClassLoaderFactory
import com.ibm.wala.classLoader.{JavaLanguage, Language}
import com.ibm.wala.ipa.callgraph.Entrypoint
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.types.{MethodReference, TypeName, TypeReference}
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConversions._

class AnalysisOptions(scope: AnalysisScope, entrypoints: java.lang.Iterable[Entrypoint], val cha: ClassHierarchy, val isSourceAnalysis: Boolean)
  extends com.ibm.wala.ipa.callgraph.AnalysisOptions(scope, entrypoints) {
}

object AnalysisOptions {

  // TODO: replace below to use the above class

  def apply(
    extraEntrypoints: Iterable[(String, String)],
    extraDependencies: Iterable[Dependency])(
      implicit config: Config): AnalysisOptions = {

    implicit val scope = AnalysisScope(extraDependencies)

    val classLoaderFactory = new EJCClassLoaderFactory(scope.getExclusions())

    implicit val cha = ClassHierarchy.make(scope, classLoaderFactory, Language.JAVA)

    new AnalysisOptions(scope, entrypoints(extraEntrypoints), cha, true) // !srcDep.isEmpty
  }

  def entrypoints(extraEntrypoints: Iterable[(String, String)] = Seq())(
    implicit config: Config, cha: ClassHierarchy, scope: AnalysisScope) = {
    val oneEntryPoint =
      if (config.hasPath("wala.entry.class"))
        Some((config.getString("wala.entry.class"), config.getString("wala.entry.method")))
      else
        None

    val entryPointsFromPattern =
      if (config.hasPath("wala.entry.signature-pattern")) {
        val signaturePattern = config.getString("wala.entry.signature-pattern")
        val matchingMethods = cha.iterator() flatMap { c =>
          c.getAllMethods() filter { m =>
            m.getSignature() matches signaturePattern
          }
        }
        matchingMethods map { new DefaultEntrypoint(_, cha) } toSeq
      } else
        Seq()

    val entrypoints = entryPointsFromPattern ++
      ((extraEntrypoints ++ oneEntryPoint) map { case (klass, method) => makeEntrypoint(klass, method) })

    if (entrypoints.size == 0)
      System.err.println("WARNING: no entrypoints")

    entrypoints
  }

  // helper apply methods

  def apply()(implicit config: Config = ConfigFactory.load): AnalysisOptions = {
    apply(Seq(), Seq())
  }

  def apply(klass: String, method: String)(implicit config: Config): AnalysisOptions = apply((klass, method), Seq())

  def apply(entrypoint: (String, String),
    dependencies: Iterable[Dependency])(implicit config: Config): AnalysisOptions = apply(Seq(entrypoint), dependencies)

  val mainMethod = "main([Ljava/lang/String;)V"

  private def makeEntrypoint(entryClass: String, entryMethod: String)(implicit scope: AnalysisScope, cha: ClassHierarchy): Entrypoint = {
    val methodReference = AnalysisScope.allScopes.toStream
      .map { scope.getLoader(_) }
      .map { TypeReference.findOrCreate(_, TypeName.string2TypeName(entryClass)) }
      .map { MethodReference.findOrCreate(_, entryMethod.substring(0, entryMethod.indexOf('(')), entryMethod.substring(entryMethod.indexOf('('))) }
      .find { cha.resolveMethod(_) != null } getOrElse { throw new Error("Could not find entrypoint: " + entryClass + "#" + entryMethod + " anywhere in loaded classes.") }

    new DefaultEntrypoint(methodReference, cha)
  }

}