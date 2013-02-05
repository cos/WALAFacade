package edu.illinois.wala.ipa.callgraph
import scala.collection.JavaConverters._
import scala.collection._
import edu.illinois.wala.Facade._
import edu.illinois.wala.S

trait ExtraFeatures { self: FlexibleCallGraphBuilder =>

  lazy val allStatements = {
    callGraph.asScala filter { _.getIR() != null } map { n => n.instructions.map(i => S(n, i)) } flatten
  }

  /**
   * find a call graph node that matches .*pattern.*
   */
  def findNode(pattern: String): Option[N] = {
    val p = (".*" + pattern + ".*")
    callGraph.asScala.find(n => n.getMethod.toString().matches(p))
  }

  /**
   * find a call graph node that matches .*pattern.*
   */
  def findNodes(pattern: String): Iterable[N] = {
    val p = (".*" + pattern + ".*")
    callGraph.asScala.filter(n => n.getMethod.toString().matches(p))
  }

  /**
   * p.pt is the set of abstract objects pointed by p
   */
  implicit def pWithPointerObjects(p: P) = new {
    def pt: Set[O] = (for (o <- heap.getSuccNodes(p).asScala) yield o.asInstanceOf[O]).toSet
  }

  /**
   * localPtTo(o) is the local pointers to o
   */
  def localPtTo(o: O): Iterable[P] = {
    (for (p <- heap.getPredNodes(o).asScala if p.isInstanceOf[P]) yield p.asInstanceOf[P]).toIterable
  }
}