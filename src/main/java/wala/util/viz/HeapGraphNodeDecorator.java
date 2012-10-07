package wala.util.viz;

import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode;
import com.ibm.wala.ipa.callgraph.propagation.ArrayContentsKey;
import com.ibm.wala.ipa.callgraph.propagation.InstanceFieldKey;
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey;
import com.ibm.wala.ipa.callgraph.propagation.PointerKey;
import com.ibm.wala.ipa.callgraph.propagation.ReturnValueKey;
import com.ibm.wala.ipa.callgraph.propagation.StaticFieldKey;
import com.ibm.wala.ipa.modref.ArrayLengthKey;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.graph.Graph;

public class HeapGraphNodeDecorator implements NodeDecorator {

	private final Graph<Object> heapGraph;

	public HeapGraphNodeDecorator(final Graph heapGraph) {
		this.heapGraph = heapGraph;
	}

	@Override
	public String getLabel(final Object obj) throws WalaException {
		if (obj instanceof AllocationSiteInNode) {
			final AllocationSiteInNode o = (AllocationSiteInNode) obj;
			return o.getSite().getDeclaredType().getName().getClassName() + " [ "
					+ o.getNode().getMethod().getName().toString() + "@" + o.getSite().getProgramCounter() + " ]";
		}
		if (obj instanceof StaticFieldKey) {
			return "STATIC: " + ((StaticFieldKey) obj).getField().getName();
		}
		if (obj instanceof InstanceFieldKey) {
			final InstanceFieldKey f = (InstanceFieldKey) obj;
			return f.getField().getName().toString();
		}
		if (obj instanceof ArrayContentsKey) {
			final ArrayContentsKey f = (ArrayContentsKey) obj;
			return "[]";
		}
		if (obj instanceof ArrayLengthKey) {
			final ArrayLengthKey f = (ArrayLengthKey) obj;
			return "ARR_LENGTH";
		}
		if (obj instanceof LocalPointerKey) {
			final LocalPointerKey p = (LocalPointerKey) obj;
			return p.getNode().getMethod().getName().toString() + "-v" + p.getValueNumber();
		}
		if (obj instanceof ReturnValueKey) {
			final ReturnValueKey p = (ReturnValueKey) obj;
			return "RET " + p.getNode().getMethod().getName();
		}

		return obj.toString();
	}

	@Override
	public String getDecoration(final Object obj) {
		if (obj instanceof LocalPointerKey)
			return "shape=diamond";
		if (obj instanceof PointerKey)
			return "shape=box";
		return "shape=oval";
	}

	@Override
	public boolean shouldDisplay(final Object n) {
		return heapGraph.getSuccNodeCount(n) > 0 || heapGraph.getPredNodeCount(n) > 0;
	}
}
