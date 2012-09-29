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
  object DependencyNature extends Enumeration {
    type DependencyNature = Value
    val Binary, BinaryDirectory, Jar, JarDirectory = Value
  }
  import DependencyNature._

  case class Dependency(file: String, nature: DependencyNature = BinaryDirectory)

  val Primordial = com.ibm.wala.ipa.callgraph.AnalysisScope.PRIMORDIAL
  val Extension = com.ibm.wala.ipa.callgraph.AnalysisScope.EXTENSION
  val Application = com.ibm.wala.ipa.callgraph.AnalysisScope.APPLICATION
  val Synthetic = com.ibm.wala.ipa.callgraph.AnalysisScope.SYNTHETIC
  def apply(jreLibPath: String, exclusionsFile: String) = new AnalysisScope(jreLibPath, exclusionsFile)
}

class AnalysisScope(jreLibPath: String, exclusionsFile: String) extends com.ibm.wala.ipa.callgraph.AnalysisScope(Collections.singleton(Language.JAVA)) {
  val UNDER_ECLIPSE = false;

  import AnalysisScope._

  addToScope(getLoader(Primordial), new JarFile(jreLibPath))

  setExclusions(FileOfClasses.createFileOfClasses(new File(exclusionsFile)))

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

  def addExtensionBinaryDependency(directory: String) {
    debug("Binary extension: " + directory)
    val sd = getFile(directory)
    assert(sd.isDirectory())
    addToScope(getLoader(Extension), new BinaryDirectoryTreeModule(sd))
  }

  def addJarFolderDependency(path: String) {
    debug("Jar folder: " + path);
    val dir = getFile(path);

    val delim = if (path.endsWith("/")) "" else "/"

    if (!dir.isDirectory())
      return ;

    val files = dir.list();
    if (files == null) return

    for (fileName <- files) {
      if (fileName.endsWith(".jar"))
        addJarDependency(path + delim + fileName);
      else {
        val file = new File(fileName)
        if (file.isDirectory())
          addJarFolderDependency(file.getAbsolutePath());
      }
    }
  }

  def addJarDependency(file: String) {
    debug("Jar: " + file);
    val M = if (UNDER_ECLIPSE)
      new FileProvider().getJarFileModule(file, getLoader());
    else
      new JarFileModule(new JarFile(file, true));

    addToScope(getLoader(Application), M);
  }

}
