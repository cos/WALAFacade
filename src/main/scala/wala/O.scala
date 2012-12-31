package wala
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode
import sppa.util.debug

object O {
  import wala.WALAConversions._
  import wala.extra.StaticClassObject

  def unapply(o: O): Option[(N, I)] = {
    o match {
      case o: AllocationSiteInNode => Some(o.getNode(), o.getNode().getIR().getNew(o.getSite()))
      case _ => None
    }
  }
  def prettyPrint(o: O) = {
    val id = printRepo.getOrElseUpdate(o, printRepo.size)
    (o match {
      case o: AllocationSiteInNode =>
        (o.getConcreteType().prettyPrint + ": " + codeLocation(o.getNode(), o.getSite().getProgramCounter())) +
          (if (debug.detailContexts) " --- " + o.getNode() else "")
      case o: StaticClassObject => "Static: " + o.klass.prettyPrint()
      case _ => o.toString
    }) + (if (debug.detailContexts) " [o " + id + " ] " else "")
  }

  val printRepo = collection.mutable.Map[O, Int]()
}