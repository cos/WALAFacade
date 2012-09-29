package wala.extra;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

case class StaticClassObject(val klass: IClass) extends InstanceKey {
  override def getConcreteType = null
}
