package wala.util.viz;

import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Context;
import com.ibm.wala.types.MethodReference;
import com.ibm.wala.util.WalaException;

public class CGNodeDecorator implements NodeDecorator {
	@Override
	public String getLabel(final Object o) throws WalaException {
		final CGNode n = (CGNode) o;
		final MethodReference reference = n.getMethod().getReference();
		String s = reference.getDeclaringClass().getName()
				+ "."
				+ reference.getSelector()
				+ " <"
				+ reference.getDeclaringClass().getClassLoader().getName()
						.toString().substring(0, 4) + ">";
		final Context context = n.getContext();

		s += context.toString();
		s = s.replace("][", "\\n");
		s = s.replace("[", "\\n");
		s = s.replace("]", "\\n");

		return s.substring(1);
	}

	@Override
	public String getDecoration(final Object n) {
		return "";
	}

	@Override
	public boolean shouldDisplay(final Object n) {
		return true;
	}

}
