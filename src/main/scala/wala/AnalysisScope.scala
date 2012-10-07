package wala

import java.io.File
import java.util.jar.JarFile
import com.ibm.wala.classLoader.BinaryDirectoryTreeModule
import com.ibm.wala.classLoader.JarFileModule
import com.ibm.wala.util.config.FileOfClasses
import com.ibm.wala.util.io.FileProvider
import scala.collection._
import scala.collection.JavaConverters._
import com.ibm.wala.util.strings.Atom
import sppa.util.debug
import java.util.Collections
import com.ibm.wala.classLoader.Language

object AnalysisScope {
  type Scope = Atom
  val Primordial = com.ibm.wala.ipa.callgraph.AnalysisScope.PRIMORDIAL
  val Extension = com.ibm.wala.ipa.callgraph.AnalysisScope.EXTENSION
  val Application = com.ibm.wala.ipa.callgraph.AnalysisScope.APPLICATION
  val Synthetic = com.ibm.wala.ipa.callgraph.AnalysisScope.SYNTHETIC
  def apply(jreLibPath: String, exclusionsFile: String) = new AnalysisScope(jreLibPath, exclusionsFile)
}

object DependencyNature extends Enumeration {
  type DependencyNature = Value
  val Binary, BinaryDirectory, Jar, JarDirectory = Value
}

import DependencyNature._
import AnalysisScope._

object Dependency {
  def apply(file: String): Dependency = apply(file, BinaryDirectory, Application)
  def apply(file: String, nature: DependencyNature): Dependency = apply(file, nature, Application)
}

case class Dependency(file: String, nature: DependencyNature, scope: Scope)

class AnalysisScope(jreLibPath: String, exclusionsFile: String, dependencies: Iterable[Dependency]) extends com.ibm.wala.ipa.callgraph.AnalysisScope(Collections.singleton(Language.JAVA)) {
  val UNDER_ECLIPSE = false;
  import AnalysisScope._

  def this(jreLibPath: String, exclusionsFile: String) = this(jreLibPath, exclusionsFile, Seq())

  initForJava()

  addToScope(getLoader(Primordial), new JarFile(jreLibPath))

  setExclusions(FileOfClasses.createFileOfClasses(new File(exclusionsFile)))

  addDependencies(dependencies)

  def addDependencies(dependencies: Iterable[Dependency]) {
    for (d <- dependencies) d match {
      case Dependency(file, BinaryDirectory, scope: Scope) => addBinaryDependency(file, scope)
      case Dependency(file, JarDirectory, scope: Scope) => addJarDirectoryDependency(file, scope)
      case Dependency(file, Jar, scope: Scope) => addJarDependency(file, scope)
      case Dependency(file, Binary, scope: Scope) => throw new Exception("Unimplemented yet")
    }
  }

  def getFile(path: String) =
    if (UNDER_ECLIPSE)
      new FileProvider().getFile(path, getLoader())
    else
      new File(path)

  def addBinaryDependency(directory: String, analysisScope: Atom = Application) {
    debug("Binary: " + directory);
    val sd = getFile(directory);
    assert(sd.isDirectory())
    addToScope(getLoader(analysisScope), new BinaryDirectoryTreeModule(sd));
  }

  def getLoader() = AnalysisScope.this.getClass().getClassLoader();

  def addJarDirectoryDependency(path: String, scope: Scope = Application) {
    debug("Jar folder: " + path);
    val dir = getFile(path);
    val delim = if (path.endsWith("/")) "" else "/"

    val files = dir.list();
    if (files == null) return

    for (fileName <- files) yield {
      if (fileName.endsWith(".jar"))
        addJarDependency(path + delim + fileName, scope)
      else {
        val file = new File(fileName)
        if (file.isDirectory())
          addJarDirectoryDependency(file.getAbsolutePath(), scope)
      }
    }
  }

  def addJarDependency(file: String, scope: Scope) {
    debug("Jar: " + file);
    val M = if (UNDER_ECLIPSE)
      new FileProvider().getJarFileModule(file, getLoader());
    else
      new JarFileModule(new JarFile(file, true));

    addToScope(getLoader(scope), M);
  }

}
