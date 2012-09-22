package wala.extra;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey;

public class StaticClassObject implements InstanceKey {
	
	private final IClass klass;

	public StaticClassObject(IClass klass) {
		this.klass = klass;
	}

	@Override
	public IClass getConcreteType() {
		return null;
	}

	public IClass getKlass() {
		return klass;
	}
	
	@Override
	public String toString() {
		return "Class: "+klass;
	}
	
	@Override
	public int hashCode() {
		return 17;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null)
			return false;
		
		if(!(obj instanceof StaticClassObject))
			return false;
		
		StaticClassObject that = (StaticClassObject) obj;
		return this.klass.equals(that.klass);
	}
}
