/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package wala.util.viz;

import com.ibm.wala.util.WalaException;

/**
 */
public interface NodeDecorator {

	public static final NodeDecorator DEFAULT = new NodeDecorator() {
		@Override
		public String getLabel(final Object o) {
			return o.toString();
		}

		@Override
		public String getDecoration(final Object n) {
			return "";
		}

		@Override
		public boolean shouldDisplay(final Object n) {
			return true;
		}

		public String getGroup(final Object n) {
			return "";
		}
	};

	/**
	 * @param o
	 * @return the String label for node o
	 */
	String getLabel(Object o) throws WalaException;

	public String getDecoration(Object n);

	public boolean shouldDisplay(Object n);

	public String getGroup(Object n);
}
