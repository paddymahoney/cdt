/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer.fast;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * @author Doug Schaefer
 *
 */
public class PDOMFastAddTU extends Job {

	private final ITranslationUnit tu;
	private final PDOM pdom;
	
	public PDOMFastAddTU(PDOM pdom, ITranslationUnit tu, IProgressMonitor group) {
		super("PDOM Fast Add: " + tu.getElementName());
		this.pdom = pdom;
		this.tu = tu;
		setPriority(PDOM.WRITER_PRIORITY);
		setProgressGroup(group, 1);
	}

	protected IStatus run(IProgressMonitor monitor) {
		try {
			ILanguage language = tu.getLanguage();
			if (language == null)
				return Status.CANCEL_STATUS;

			// begin the rule before the parse so that we are only parsing once at a time
			// maybe we can do more than one some day
			getJobManager().beginRule(pdom.getWriterLockRule(), monitor);
			monitor.beginTask("Adding: " + tu.getElementName(), 1);		
			
			// get the AST in a "Fast" way
			IASTTranslationUnit ast = language.getASTTranslationUnit(tu,
					ILanguage.AST_USE_INDEX |
					ILanguage.AST_SKIP_INDEXED_HEADERS |
					ILanguage.AST_SKIP_IF_NO_BUILD_INFO);
			if (ast == null)
				return Status.CANCEL_STATUS;
			
			pdom.addSymbols(language, ast);
			monitor.done();
			getJobManager().endRule(pdom.getWriterLockRule());
			return Status.OK_STATUS;
		} catch (CoreException e) {
			return e.getStatus();
		}
	}

}
