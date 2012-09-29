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
package util.wala.viz;

import java.io.IOException;
import java.util.Arrays;

import com.ibm.wala.util.WalaException;

/**
 * Launch gsview on a postscript file
 * 
 * TODO: inherit from a launcher?
 */
class PDFViewLauncher {

	private Process process;

	/**
	 * Name of the postscript file to view
	 */
	protected String pdffile = null;

	/**
	 * Path to ghostview executable
	 */
	protected String gvExe = null;

	PDFViewLauncher() {
		super();
	}

	String getPDFFile() {
		return pdffile;
	}

	void setPDFFile(final String newPsfile) {
		pdffile = newPsfile;
	}

	String getGvExe() {
		return gvExe;
	}

	void setGvExe(final String newGvExe) {
		gvExe = newGvExe;
	}

	@Override
	public String toString() {
		final StringBuffer result = new StringBuffer(super.toString());
		result.append(", psfile: ");
		result.append(pdffile);
		result.append(", gvExe: ");
		result.append(gvExe);
		result.append(')');
		return result.toString();
	}

	private WalaException exception = null;

	/*
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		final String[] cmdarray = { getGvExe(), getPDFFile() };
		try {
			final Process p = Runtime.getRuntime().exec(cmdarray);
			setProcess(p);
		} catch (final IOException e) {
			e.printStackTrace();
			exception = new WalaException("gv invocation failed for\n" + Arrays.toString(cmdarray));
		}
	}

	public WalaException getException() {
		return exception;
	}

	public Process getProcess() {
		return process;
	}

	public void setProcess(final Process process) {
		this.process = process;
	}
}
