package org.illinois.wala

// remember that
// type N = CGNode // call graph nodes
// type PutI = SSAPutInstruction
// type LocalP = LocalPointerKey

import com.ibm.wala.ipa.callgraph.impl.ContextInsensitiveSelector
import com.ibm.wala.ipa.callgraph.propagation.cfa.nCFAContextSelector
import com.ibm.wala.util.graph.traverse.DFS
import com.typesafe.config.{ConfigResolveOptions, ConfigFactory}
import edu.illinois.wala.ipa.callgraph.FlexibleCallGraphBuilder
import edu.illinois.wala.ipa.callgraph.propagation.P

// convenience object that activates all implicit converters
import edu.illinois.wala.Facade._

import scala.collection.JavaConversions._

object Test extends App {
  implicit val config = ConfigFactory.parseString("""
  wala {
    jre-lib-path = "/Library/Java/JavaVirtualMachines/jdk1.8.0_45.jdk/Contents/Home/jre/lib/rt.jar"
    dependencies.binary += "target/scala-2.11/test-classes"
    exclussions = ""
    entry {
      signature-pattern = ".*Foo.*main.*"
    }
  }
  """).resolve()

  // creates a new pointer analysis with a special context selector
  // implicitly uses the above config file
  val pa = new FlexibleCallGraphBuilder() {
    override def cs = new nCFAContextSelector(2, new ContextInsensitiveSelector());
  }
  import pa._

  // make cg, heap, etc. available in scope

  val startNodes = cg filter { n: N => n.m.name == "bar" }
  val reachableNodes = DFS.getReachableNodes(cg, startNodes)
  val foo = reachableNodes flatMap { n =>
    n.instructions collect {
      case i: PutI =>
        val p: LocalP = P(n, i.v)
        val variableNames: Iterable[String] = p.variableNames()
        val fieldName: F = i.f.get
        (fieldName, variableNames)
    }
  }

  // and a 3-liner
  DFS
    .getReachableNodes(cg, cg filter { _.m.name == "bar" })
    .flatMap { n => n.instructions collect { case i: PutI => (i.f.get, P(n, i.v).variableNames()) } }
}
