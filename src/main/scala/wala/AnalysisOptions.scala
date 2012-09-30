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

class AnalysisOptions(scope: AnalysisScope, entrypoints: java.lang.Iterable[Entrypoint], val cha: ClassHierarchy)
  extends com.ibm.wala.ipa.callgraph.AnalysisOptions(scope, entrypoints)

object AnalysisOptions {
  val config = ConfigFactory.load()

  def apply(
    entrypoints: Iterable[(String, String)],
    scope: AnalysisScope) = {

    implicit val s = scope
    implicit val cha = ClassHierarchy.make(scope)

    val entrypointsW = entrypoints map { case (klass, method) => makeEntrypoint(klass, method) } asJava

    new AnalysisOptions(scope, entrypointsW, cha)
  }

  def apply(
    entrypoints: Iterable[(String, String)],
    dependencies: Iterable[Dependency]): AnalysisOptions = {

    val scope = new AnalysisScope(config.getString("wala.jre-lib-path"), config.getString("wala.exclussions-file"))
    for (d <- dependencies) d match {
      case Dependency(file, DependencyNature.BinaryDirectory) => scope.addBinaryDependency(file)
    }
    
    apply(entrypoints, scope)
  }

  def makeEntrypoint(entryClass: String, entryMethod: String)(implicit scope: AnalysisScope, cha: ClassHierarchy): Entrypoint = {
    val typeReference: TypeReference = TypeReference.findOrCreate(scope.getLoader(AnalysisScope.Application),
      TypeName.string2TypeName(entryClass))
    val methodReference: MethodReference = MethodReference.findOrCreate(typeReference,
      entryMethod.substring(0, entryMethod.indexOf('(')), entryMethod.substring(entryMethod.indexOf('(')))
    new DefaultEntrypoint(methodReference, cha)
  }
}