/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package pl.greenpath.mockito.ide.refactoring;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;


public class JavaTestPlugin extends Plugin {

	private static JavaTestPlugin fgDefault;

	public JavaTestPlugin() {
		fgDefault= this;
	}

	public static JavaTestPlugin getDefault() {
		return fgDefault;
	}

	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public File getFileInPlugin(final IPath path) throws CoreException {
		try {
			final URL installURL= new URL(getBundle().getEntry("/"), path.toString());
			final URL localURL= FileLocator.toFileURL(installURL);
			return new File(localURL.getFile());
		} catch (final IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, getPluginId(), IStatus.ERROR, e.getMessage(), e));
		}
	}

	public static String getPluginId() {
		return "org.eclipse.jdt.ui.tests";
	}

	public static void log(final IStatus status) {
		getDefault().getLog().log(status);
	}

	public static void logErrorMessage(final String message) {
		log(new Status(IStatus.ERROR, getPluginId(), IStatus.ERROR, message, null));
	}

	public static void logErrorStatus(final String message, final IStatus status) {
		if (status == null) {
			logErrorMessage(message);
			return;
		}
		final MultiStatus multi= new MultiStatus(getPluginId(), IStatus.ERROR, message, null);
		multi.add(status);
		log(multi);
	}

	public static void log(final Throwable e) {
		log(new Status(IStatus.ERROR, getPluginId(), IStatus.ERROR, e.getMessage(), e));
	}



}
