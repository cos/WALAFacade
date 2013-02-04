package wala.extra;

import com.ibm.wala.classLoader.IClass
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey
import com.ibm.wala.ipa.callgraph.CallGraph
import com.ibm.wala.util.collections.EmptyIterator

case class StaticClassObject(val klass: IClass) extends InstanceKey {
  override def getCreationSites(cg: CallGraph) = EmptyIterator.instance()
  override def getConcreteType = null
}
