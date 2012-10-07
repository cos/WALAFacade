package wala.util.viz;

import com.ibm.wala.util.collections.Filter;
import com.ibm.wala.util.graph.Graph;

public class ColoredHeapGraphNodeDecorator extends HeapGraphNodeDecorator {

	private final Filter<Object> filter;

	@SuppressWarnings("deprecation")
	public ColoredHeapGraphNodeDecorator(final Graph<Object> heapGraph, final Filter<Object> filter) {
		super(heapGraph);
		this.filter = filter;
	}

	@Override
	public String getDecoration(final Object obj) {
		if (filter.accepts(obj))
			return super.getDecoration(obj) + ", style=filled, fillcolor=darkseagreen1";
		else
			return super.getDecoration(obj);
	}
}
