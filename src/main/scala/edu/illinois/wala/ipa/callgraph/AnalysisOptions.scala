package edu.illinois.wala.ipa.callgraph

import com.typesafe.config.ConfigFactory
import com.ibm.wala.ipa.cha.ClassHierarchy
import com.ibm.wala.types.TypeReference
import com.ibm.wala.types.MethodReference
import com.ibm.wala.types.TypeName
import com.ibm.wala.ipa.callgraph.impl.DefaultEntrypoint
import com.ibm.wala.ipa.callgraph.Entrypoint
import scala.collection.JavaConversions._
import com.typesafe.config.Config
import com.ibm.wala.classLoader.ClassLoaderFactoryImpl
import com.ibm.wala.classLoader.ClassLoaderFactory
import com.ibm.wala.types.ClassLoaderReference
import com.typesafe.config.ConfigList

class AnalysisOptions(scope: AnalysisScope, entrypoints: java.lang.Iterable[Entrypoint], val cha: ClassHierarchy, val isSourceAnalysis: Boolean)
  extends com.ibm.wala.ipa.callgraph.AnalysisOptions(scope, entrypoints) {
}

object AnalysisOptions {

  implicit class RichConfig(c: Config) {
    def getListOption(path: String): Option[ConfigList] =
      if (c.hasPath(path))
        Some(c.getList(path))
      else
        None

    def getStringOption(path: String): Option[String] =
      if (c.hasPath(path))
        Some(c.getString(path))
      else
        None
  }

  // TODO: replace below to use the above class

  def apply(
    extraEntrypoints: Iterable[(String, String)],
    extraDependencies: Iterable[Dependency])(
      implicit config: Config): AnalysisOptions = {

    val binDep = if (config.hasPath("wala.dependencies.binary"))
      config.getList("wala.dependencies.binary") map { d => Dependency(d.unwrapped.asInstanceOf[String]) }
    else
      List()

    val srcDep = if (config.hasPath("wala.dependencies.source"))
      config.getList("wala.dependencies.source") map { d => Dependency(d.unwrapped.asInstanceOf[String], DependencyNature.SourceDirectory) }
    else
      List()

    val jarDep = if (config.hasPath("wala.dependencies.jar"))
      config.getList("wala.dependencies.jar") map { d => Dependency(d.unwrapped.asInstanceOf[String], DependencyNature.Jar) }
    else
      List()

    val dependencies = binDep ++ srcDep ++ jarDep ++ extraDependencies

    val jreLibPath = if (config.hasPath("wala.jre-lib-path"))
      config.getString("wala.jre-lib-path")
    else
      System.getenv().get("JAVA_HOME") + "/jre/lib/rt.jar"

    implicit val scope = new AnalysisScope(jreLibPath, config.getString("wala.exclussions"), dependencies)

    val classLoaderImpl = new ClassLoaderFactoryImpl(scope.getExclusions())
    //      if (!srcDep.isEmpty) 
    //      new PolyglotClassLoaderFactory(scope.getExclusions(), new JavaIRTranslatorExtension())
    //    else

    implicit val cha = ClassHierarchy.make(scope, classLoaderImpl)

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
      
    if(entrypoints.size == 0)
      System.err.println("WARNING: no entrypoints")

    new AnalysisOptions(scope, entrypoints, cha, !srcDep.isEmpty)
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