package pl.greenpath.mockito.ide.refactoring;

/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Ferenc Hechler, ferenc_hechler@users.sourceforge.net - 83258 [jar exporter] Deploy java application as executable jar
 *******************************************************************************/
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Map;

import junit.framework.TestCase;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.util.CoreUtility;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Synchronizer;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.osgi.framework.Bundle;

/**
 * Helper methods to set up a IJavaProject.
 */
public class JavaProjectHelper {

    /**
     * XXX: Flag to enable/disable dummy search to synchronize with indexer. See
     * https://bugs.eclipse.org/391927 .
     */
    private static final boolean PERFORM_DUMMY_SEARCH = false;

    /**
     * @deprecated use {@link #RT_STUBS_15}
     */
    @Deprecated
    public static final IPath RT_STUBS_13 = new Path("testresources/rtstubs.jar");
    /**
     * @deprecated use {@link #JUNIT_SRC_381}
     */
    @Deprecated
    public static final IPath JUNIT_SRC = new Path("testresources/junit37-noUI-src.zip");

    public static final IPath RT_STUBS_15 = new Path("testresources/rtstubs15.jar");
    public static final IPath RT_STUBS_16 = new Path("testresources/rtstubs16.jar");
    public static final IPath RT_STUBS_17 = new Path("testresources/rtstubs17.jar");
    public static final IPath JUNIT_SRC_381 = new Path("testresources/junit381-noUI-src.zip");
    public static final String JUNIT_SRC_ENCODING = "ISO-8859-1";

    public static final IPath MYLIB = new Path("testresources/mylib.jar");
    public static final IPath MYLIB_STDOUT = new Path("testresources/mylib_stdout.jar");
    public static final IPath MYLIB_SIG = new Path("testresources/mylib_sig.jar");
    public static final IPath NLS_LIB = new Path("testresources/nls.jar");

    private static final int MAX_RETRY = 5;
    private static final int RETRY_DELAY = 1000;

    public static final int COUNT_CLASSES_RT_STUBS_15 = 661;
    public static final int COUNT_INTERFACES_RT_STUBS_15 = 135;

    public static final int COUNT_CLASSES_JUNIT_SRC_381 = 76;
    public static final int COUNT_INTERFACES_JUNIT_SRC_381 = 8;
    public static final int COUNT_CLASSES_MYLIB = 3;

    /**
     * If set to <code>true</code> all resources that are deleted using
     * {@link #delete(IJavaElement)} and that contain mixed line delimiters will
     * result in a test failure.
     * <p>
     * Should be <code>false</code> during normal and Releng test runs due to
     * performance impact and because the search plug-in gets loaded which
     * results in a test failure.
     * </p>
     */
    private static final boolean ASSERT_NO_MIXED_LINE_DELIMIERS = false;

    /**
     * Creates a IJavaProject.
     * 
     * @param projectName
     *            The name of the project
     * @param binFolderName
     *            Name of the output folder
     * @return Returns the Java project handle
     * @throws CoreException
     *             Project creation failed
     */
    public static IJavaProject createJavaProject(final String projectName, final String binFolderName)
            throws CoreException {
        final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
        final IProject project = root.getProject(projectName);
        if (!project.exists()) {
            project.create(null);
        } else {
            project.refreshLocal(IResource.DEPTH_INFINITE, null);
        }

        if (!project.isOpen()) {
            project.open(null);
        }

        IPath outputLocation;
        if (binFolderName != null && binFolderName.length() > 0) {
            final IFolder binFolder = project.getFolder(binFolderName);
            if (!binFolder.exists()) {
                CoreUtility.createFolder(binFolder, false, true, null);
            }
            outputLocation = binFolder.getFullPath();
        } else {
            outputLocation = project.getFullPath();
        }

        if (!project.hasNature(JavaCore.NATURE_ID)) {
            addNatureToProject(project, JavaCore.NATURE_ID, null);
        }

        final IJavaProject jproject = JavaCore.create(project);

        jproject.setOutputLocation(outputLocation, null);
        jproject.setRawClasspath(new IClasspathEntry[0], null);

        return jproject;
    }

    /**
     * Sets the compiler options to 1.7 for the given project.
     * 
     * @param project
     *            the java project
     */
    public static void set17CompilerOptions(final IJavaProject project) {
        final Map options = project.getOptions(false);
        JavaProjectHelper.set17CompilerOptions(options);
        project.setOptions(options);
    }

    /**
     * Sets the compiler options to 1.6 for the given project.
     * 
     * @param project
     *            the java project
     */
    public static void set16CompilerOptions(final IJavaProject project) {
        final Map options = project.getOptions(false);
        JavaProjectHelper.set16CompilerOptions(options);
        project.setOptions(options);
    }

    /**
     * Sets the compiler options to 1.5 for the given project.
     * 
     * @param project
     *            the java project
     */
    public static void set15CompilerOptions(final IJavaProject project) {
        final Map options = project.getOptions(false);
        JavaProjectHelper.set15CompilerOptions(options);
        project.setOptions(options);
    }

    /**
     * Sets the compiler options to 1.4 for the given project.
     * 
     * @param project
     *            the java project
     */
    public static void set14CompilerOptions(final IJavaProject project) {
        final Map options = project.getOptions(false);
        JavaProjectHelper.set14CompilerOptions(options);
        project.setOptions(options);
    }

    /**
     * Sets the compiler options to 1.7
     * 
     * @param options
     *            The compiler options to configure
     */
    public static void set17CompilerOptions(final Map options) {
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
    }

    /**
     * Sets the compiler options to 1.6
     * 
     * @param options
     *            The compiler options to configure
     */
    public static void set16CompilerOptions(final Map options) {
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_6, options);
    }

    /**
     * Sets the compiler options to 1.5
     * 
     * @param options
     *            The compiler options to configure
     */
    public static void set15CompilerOptions(final Map options) {
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_5, options);
    }

    /**
     * Sets the compiler options to 1.4
     * 
     * @param options
     *            The compiler options to configure
     */
    public static void set14CompilerOptions(final Map options) {
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_4, options);
    }

    /**
     * Sets the compiler options to 1.3
     * 
     * @param options
     *            The compiler options to configure
     */
    public static void set13CompilerOptions(final Map options) {
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_3, options);
    }

    /**
     * Removes an IJavaElement's resource. Retries if deletion failed (e.g.
     * because the indexer still locks the file).
     * 
     * @param elem
     *            the element to delete
     * @throws CoreException
     *             if operation failed
     * @see #ASSERT_NO_MIXED_LINE_DELIMIERS
     */
    public static void delete(final IJavaElement elem) throws CoreException {
        if (ASSERT_NO_MIXED_LINE_DELIMIERS)
            MixedLineDelimiterDetector.assertNoMixedLineDelimiters(elem);

        final IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            @Override
            public void run(final IProgressMonitor monitor) throws CoreException {
                performDummySearch();
                if (elem instanceof IJavaProject) {
                    final IJavaProject jproject = (IJavaProject) elem;
                    jproject.setRawClasspath(new IClasspathEntry[0], jproject.getProject().getFullPath(), null);
                }
                delete(elem.getResource());
            }
        };
        ResourcesPlugin.getWorkspace().run(runnable, null);
        emptyDisplayLoop();
    }

    /**
     * Removes a resource. Retries if deletion failed (e.g. because the indexer
     * still locks the file).
     * 
     * @param resource
     *            the resource to delete
     * @throws CoreException
     *             if operation failed
     */
    public static void delete(final IResource resource) throws CoreException {
        for (int i = 0; i < MAX_RETRY; i++) {
            try {
                resource.delete(IResource.FORCE | IResource.ALWAYS_DELETE_PROJECT_CONTENT, null);
                i = MAX_RETRY;
            } catch (final CoreException e) {
                if (i == MAX_RETRY - 1) {
                    JavaPlugin.log(e);
                    throw e;
                }
                try {
                    JavaPlugin.log(new IllegalStateException("sleep before retrying JavaProjectHelper.delete() for "
                            + resource.getLocationURI()));
                    Thread.sleep(RETRY_DELAY); // give other threads time to
                                               // close the file
                } catch (final InterruptedException e1) {
                }
            }
        }
    }

    /**
     * Removes a package fragment. Retries if deletion failed (e.g. because the
     * indexer still locks a file).
     * 
     * @param pack
     *            the package to delete
     * @throws CoreException
     *             if operation failed
     */
    public static void deletePackage(final IPackageFragment pack) throws CoreException {
        for (int i = 0; i < MAX_RETRY; i++) {
            try {
                pack.delete(true, null);
                i = MAX_RETRY;
            } catch (final CoreException e) {
                if (i == MAX_RETRY - 1) {
                    JavaPlugin.log(e);
                    throw e;
                }
                try {
                    JavaPlugin.log(new IllegalStateException(
                            "sleep before retrying JavaProjectHelper.delete() for package "
                                    + pack.getHandleIdentifier()));
                    Thread.sleep(RETRY_DELAY); // give other threads time to
                                               // close the file
                } catch (final InterruptedException e1) {
                }
            }
        }
    }

    /**
     * Removes all files in the project and sets the given classpath
     * 
     * @param jproject
     *            The project to clear
     * @param entries
     *            The default class path to set
     * @throws Exception
     *             Clearing the project failed
     */
    public static void clear(final IJavaProject jproject, final IClasspathEntry[] entries) throws Exception {
        performDummySearch();
        final IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
            @Override
            public void run(final IProgressMonitor monitor) throws CoreException {
                jproject.setRawClasspath(entries, null);

                final IResource[] resources = jproject.getProject().members();
                for (int i = 0; i < resources.length; i++) {
                    if (!resources[i].getName().startsWith(".")) {
                        delete(resources[i]);
                    }
                }
            }
        };
        ResourcesPlugin.getWorkspace().run(runnable, null);

        JavaProjectHelper.emptyDisplayLoop();
    }

    public static void mustPerformDummySearch() throws JavaModelException {
        performDummySearch(SearchEngine.createWorkspaceScope(), true);
    }

    public static void mustPerformDummySearch(final IJavaElement element) throws JavaModelException {
        performDummySearch(SearchEngine.createJavaSearchScope(new IJavaElement[] { element }), true);
    }

    public static void performDummySearch() throws JavaModelException {
        performDummySearch(SearchEngine.createWorkspaceScope(), PERFORM_DUMMY_SEARCH);
    }

    public static void performDummySearch(final IJavaElement element) throws JavaModelException {
        performDummySearch(SearchEngine.createJavaSearchScope(new IJavaElement[] { element }), PERFORM_DUMMY_SEARCH);
    }

    private static void performDummySearch(final IJavaSearchScope searchScope, final boolean doIt)
            throws JavaModelException {
        /*
         * Workaround for intermittent test failures. The problem is that the
         * Java indexer may still be reading a file that has just been created,
         * but a test already tries to delete the file again.
         * 
         * This can theoretically also happen in real life, but it's expected to
         * be very rare, and there's no good solution for the problem, since the
         * Java indexer should not take a workspace lock for these files.
         * 
         * performDummySearch() was found to be a performance bottleneck, so
         * we've disabled it in most situations. Use a mustPerformDummySearch()
         * method if you really need it and you can't use a delete(..) method
         * that retries a few times before failing.
         */
        if (!doIt)
            return;

        new SearchEngine().searchAllTypeNames(
                null,
                SearchPattern.R_EXACT_MATCH,
                "XXXXXXXXX".toCharArray(), // make sure we search a concrete
                                           // name. This is faster according to
                                           // Kent
                SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE,
                IJavaSearchConstants.CLASS,
                searchScope,
                new Requestor(),
                IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
                null);
    }

    /**
     * Adds a source container to a IJavaProject.
     * 
     * @param jproject
     *            The parent project
     * @param containerName
     *            The name of the new source container
     * @return The handle to the new source container
     * @throws CoreException
     *             Creation failed
     */
    public static IPackageFragmentRoot addSourceContainer(final IJavaProject jproject, final String containerName)
            throws CoreException {
        return addSourceContainer(jproject, containerName, new Path[0]);
    }

    /**
     * Adds a source container to a IJavaProject.
     * 
     * @param jproject
     *            The parent project
     * @param containerName
     *            The name of the new source container
     * @param exclusionFilters
     *            Exclusion filters to set
     * @return The handle to the new source container
     * @throws CoreException
     *             Creation failed
     */
    public static IPackageFragmentRoot addSourceContainer(final IJavaProject jproject, final String containerName,
            final IPath[] exclusionFilters) throws CoreException {
        return addSourceContainer(jproject, containerName, new Path[0], exclusionFilters);
    }

    /**
     * Adds a source container to a IJavaProject.
     * 
     * @param jproject
     *            The parent project
     * @param containerName
     *            The name of the new source container
     * @param inclusionFilters
     *            Inclusion filters to set
     * @param exclusionFilters
     *            Exclusion filters to set
     * @return The handle to the new source container
     * @throws CoreException
     *             Creation failed
     */
    public static IPackageFragmentRoot addSourceContainer(final IJavaProject jproject, final String containerName,
            final IPath[] inclusionFilters, final IPath[] exclusionFilters) throws CoreException {
        return addSourceContainer(jproject, containerName, inclusionFilters, exclusionFilters, null);
    }

    /**
     * Adds a source container to a IJavaProject.
     * 
     * @param jproject
     *            The parent project
     * @param containerName
     *            The name of the new source container
     * @param inclusionFilters
     *            Inclusion filters to set
     * @param exclusionFilters
     *            Exclusion filters to set
     * @param outputLocation
     *            The location where class files are written to, <b>null</b> for
     *            project output folder
     * @return The handle to the new source container
     * @throws CoreException
     *             Creation failed
     */
    public static IPackageFragmentRoot addSourceContainer(final IJavaProject jproject, final String containerName,
            final IPath[] inclusionFilters, final IPath[] exclusionFilters, final String outputLocation)
            throws CoreException {
        final IProject project = jproject.getProject();
        IContainer container = null;
        if (containerName == null || containerName.length() == 0) {
            container = project;
        } else {
            final IFolder folder = project.getFolder(containerName);
            if (!folder.exists()) {
                CoreUtility.createFolder(folder, false, true, null);
            }
            container = folder;
        }
        final IPackageFragmentRoot root = jproject.getPackageFragmentRoot(container);

        IPath outputPath = null;
        if (outputLocation != null) {
            final IFolder folder = project.getFolder(outputLocation);
            if (!folder.exists()) {
                CoreUtility.createFolder(folder, false, true, null);
            }
            outputPath = folder.getFullPath();
        }
        final IClasspathEntry cpe = JavaCore.newSourceEntry(root.getPath(), inclusionFilters, exclusionFilters,
                outputPath);
        addToClasspath(jproject, cpe);
        return root;
    }

    /**
     * Removes a source folder from a IJavaProject.
     * 
     * @param jproject
     *            The parent project
     * @param containerName
     *            Name of the source folder to remove
     * @throws CoreException
     *             Remove failed
     */
    public static void removeSourceContainer(final IJavaProject jproject, final String containerName)
            throws CoreException {
        final IFolder folder = jproject.getProject().getFolder(containerName);
        removeFromClasspath(jproject, folder.getFullPath());
        folder.delete(true, null);
    }

    /**
     * Adds a library entry to a IJavaProject.
     * 
     * @param jproject
     *            The parent project
     * @param path
     *            The path of the library to add
     * @return The handle of the created root
     * @throws JavaModelException
     */
    public static IPackageFragmentRoot addLibrary(final IJavaProject jproject, final IPath path)
            throws JavaModelException {
        return addLibrary(jproject, path, null, null);
    }

    /**
     * Adds a library entry with source attachment to a IJavaProject.
     * 
     * @param jproject
     *            The parent project
     * @param path
     *            The path of the library to add
     * @param sourceAttachPath
     *            The source attachment path
     * @param sourceAttachRoot
     *            The source attachment root path
     * @return The handle of the created root
     * @throws JavaModelException
     */
    public static IPackageFragmentRoot addLibrary(final IJavaProject jproject, final IPath path,
            final IPath sourceAttachPath, final IPath sourceAttachRoot) throws JavaModelException {
        final IClasspathEntry cpe = JavaCore.newLibraryEntry(path, sourceAttachPath, sourceAttachRoot);
        addToClasspath(jproject, cpe);
        final IResource workspaceResource = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
        if (workspaceResource != null) {
            return jproject.getPackageFragmentRoot(workspaceResource);
        }
        return jproject.getPackageFragmentRoot(path.toString());
    }

    /**
     * Copies the library into the project and adds it as library entry.
     * 
     * @param jproject
     *            The parent project
     * @param jarPath
     * @param sourceAttachPath
     *            The source attachment path
     * @param sourceAttachRoot
     *            The source attachment root path
     * @return The handle of the created root
     * @throws IOException
     * @throws CoreException
     */
    public static IPackageFragmentRoot addLibraryWithImport(final IJavaProject jproject, final IPath jarPath,
            final IPath sourceAttachPath, final IPath sourceAttachRoot) throws IOException, CoreException {
        final IProject project = jproject.getProject();
        final IFile newFile = project.getFile(jarPath.lastSegment());
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(jarPath.toFile());
            newFile.create(inputStream, true, null);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (final IOException e) {
                }
            }
        }
        return addLibrary(jproject, newFile.getFullPath(), sourceAttachPath, sourceAttachRoot);
    }

    /**
     * Creates and adds a class folder to the class path.
     * 
     * @param jproject
     *            The parent project
     * @param containerName
     * @param sourceAttachPath
     *            The source attachment path
     * @param sourceAttachRoot
     *            The source attachment root path
     * @return The handle of the created root
     * @throws CoreException
     */
    public static IPackageFragmentRoot addClassFolder(final IJavaProject jproject, final String containerName,
            final IPath sourceAttachPath, final IPath sourceAttachRoot) throws CoreException {
        final IProject project = jproject.getProject();
        IContainer container = null;
        if (containerName == null || containerName.length() == 0) {
            container = project;
        } else {
            final IFolder folder = project.getFolder(containerName);
            if (!folder.exists()) {
                CoreUtility.createFolder(folder, false, true, null);
            }
            container = folder;
        }
        final IClasspathEntry cpe = JavaCore.newLibraryEntry(container.getFullPath(), sourceAttachPath,
                sourceAttachRoot);
        addToClasspath(jproject, cpe);
        return jproject.getPackageFragmentRoot(container);
    }

    /**
     * Adds a library entry pointing to a JRE (stubs only) and sets the right
     * compiler options.
     * <p>
     * Currently, the compiler compliance level is 1.5.
     * 
     * @param jproject
     *            target
     * @return the new package fragment root
     * @throws CoreException
     */
    public static IPackageFragmentRoot addRTJar(final IJavaProject jproject) throws CoreException {
        return addRTJar15(jproject);
    }

    public static IPackageFragmentRoot addRTJar13(final IJavaProject jproject) throws CoreException {
        final IPath[] rtJarPath = findRtJar(RT_STUBS_13);

        final Map options = jproject.getOptions(false);
        JavaProjectHelper.set13CompilerOptions(options);
        jproject.setOptions(options);

        return addLibrary(jproject, rtJarPath[0], rtJarPath[1], rtJarPath[2]);
    }

    public static IPackageFragmentRoot addRTJar15(final IJavaProject jproject) throws CoreException, JavaModelException {
        final IPath[] rtJarPath = findRtJar(RT_STUBS_15);
        set15CompilerOptions(jproject);
        return addLibrary(jproject, rtJarPath[0], rtJarPath[1], rtJarPath[2]);
    }

    public static IPackageFragmentRoot addRTJar16(final IJavaProject jproject) throws CoreException {
        final IPath[] rtJarPath = findRtJar(RT_STUBS_16);
        set16CompilerOptions(jproject);
        return addLibrary(jproject, rtJarPath[0], rtJarPath[1], rtJarPath[2]);
    }

    public static IPackageFragmentRoot addRTJar17(final IJavaProject jproject) throws CoreException {
        final IPath[] rtJarPath = findRtJar(RT_STUBS_17);
        set17CompilerOptions(jproject);
        return addLibrary(jproject, rtJarPath[0], rtJarPath[1], rtJarPath[2]);
    }

    /**
     * Adds a variable entry with source attachment to a IJavaProject. Can
     * return null if variable can not be resolved.
     * 
     * @param jproject
     *            The parent project
     * @param path
     *            The variable path
     * @param sourceAttachPath
     *            The source attachment path (variable path)
     * @param sourceAttachRoot
     *            The source attachment root path (variable path)
     * @return The added package fragment root
     * @throws JavaModelException
     */
    public static IPackageFragmentRoot addVariableEntry(final IJavaProject jproject, final IPath path,
            final IPath sourceAttachPath, final IPath sourceAttachRoot) throws JavaModelException {
        final IClasspathEntry cpe = JavaCore.newVariableEntry(path, sourceAttachPath, sourceAttachRoot);
        addToClasspath(jproject, cpe);
        final IPath resolvedPath = JavaCore.getResolvedVariablePath(path);
        if (resolvedPath != null) {
            return jproject.getPackageFragmentRoot(resolvedPath.toString());
        }
        return null;
    }

    public static IPackageFragmentRoot addVariableRTJar13(final IJavaProject jproject, final String libVarName,
            final String srcVarName, final String srcrootVarName) throws CoreException {
        return addVariableRTJar(jproject, RT_STUBS_13, libVarName, srcVarName, srcrootVarName);
    }

    /**
     * Adds a variable entry pointing to a current JRE (stubs only) and sets the
     * compiler compliance level on the project accordingly. The arguments
     * specify the names of the variables to be used. Currently, the compiler
     * compliance level is set to 1.5.
     * 
     * @param jproject
     *            the project to add the variable RT JAR
     * @param libVarName
     *            Name of the variable for the library
     * @param srcVarName
     *            Name of the variable for the source attachment. Can be
     *            <code>null</code>.
     * @param srcrootVarName
     *            name of the variable for the source attachment root. Can be
     *            <code>null</code>.
     * @return the new package fragment root
     * @throws CoreException
     *             Creation failed
     */
    public static IPackageFragmentRoot addVariableRTJar(final IJavaProject jproject, final String libVarName,
            final String srcVarName, final String srcrootVarName) throws CoreException {
        return addVariableRTJar(jproject, RT_STUBS_15, libVarName, srcVarName, srcrootVarName);
    }

    /**
     * Adds a variable entry pointing to a current JRE (stubs only). The
     * arguments specify the names of the variables to be used. Clients must not
     * forget to set the right compiler compliance level on the project.
     * 
     * @param jproject
     *            the project to add the variable RT JAR
     * @param rtStubsPath
     *            path to an rt.jar
     * @param libVarName
     *            name of the variable for the library
     * @param srcVarName
     *            Name of the variable for the source attachment. Can be
     *            <code>null</code>.
     * @param srcrootVarName
     *            Name of the variable for the source attachment root. Can be
     *            <code>null</code>.
     * @return the new package fragment root
     * @throws CoreException
     *             Creation failed
     */
    private static IPackageFragmentRoot addVariableRTJar(final IJavaProject jproject, final IPath rtStubsPath,
            final String libVarName, final String srcVarName, final String srcrootVarName) throws CoreException {
        final IPath[] rtJarPaths = findRtJar(rtStubsPath);
        final IPath libVarPath = new Path(libVarName);
        IPath srcVarPath = null;
        IPath srcrootVarPath = null;
        JavaCore.setClasspathVariable(libVarName, rtJarPaths[0], null);
        if (srcVarName != null) {
            final IPath varValue = rtJarPaths[1] != null ? rtJarPaths[1] : Path.EMPTY;
            JavaCore.setClasspathVariable(srcVarName, varValue, null);
            srcVarPath = new Path(srcVarName);
        }
        if (srcrootVarName != null) {
            final IPath varValue = rtJarPaths[2] != null ? rtJarPaths[2] : Path.EMPTY;
            JavaCore.setClasspathVariable(srcrootVarName, varValue, null);
            srcrootVarPath = new Path(srcrootVarName);
        }
        return addVariableEntry(jproject, libVarPath, srcVarPath, srcrootVarPath);
    }

    /**
     * Adds a required project entry.
     * 
     * @param jproject
     *            Parent project
     * @param required
     *            Project to add to the build path
     * @throws JavaModelException
     *             Creation failed
     */
    public static void addRequiredProject(final IJavaProject jproject, final IJavaProject required)
            throws JavaModelException {
        final IClasspathEntry cpe = JavaCore.newProjectEntry(required.getProject().getFullPath());
        addToClasspath(jproject, cpe);
    }

    public static void removeFromClasspath(final IJavaProject jproject, final IPath path) throws JavaModelException {
        final IClasspathEntry[] oldEntries = jproject.getRawClasspath();
        final int nEntries = oldEntries.length;
        final ArrayList list = new ArrayList(nEntries);
        for (int i = 0; i < nEntries; i++) {
            final IClasspathEntry curr = oldEntries[i];
            if (!path.equals(curr.getPath())) {
                list.add(curr);
            }
        }
        final IClasspathEntry[] newEntries = (IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]);
        jproject.setRawClasspath(newEntries, null);
    }

    public static void addToClasspath(final IJavaProject jproject, final IClasspathEntry cpe) throws JavaModelException {
        final IClasspathEntry[] oldEntries = jproject.getRawClasspath();
        for (int i = 0; i < oldEntries.length; i++) {
            if (oldEntries[i].equals(cpe)) {
                return;
            }
        }
        final int nEntries = oldEntries.length;
        final IClasspathEntry[] newEntries = new IClasspathEntry[nEntries + 1];
        System.arraycopy(oldEntries, 0, newEntries, 0, nEntries);
        newEntries[nEntries] = cpe;
        jproject.setRawClasspath(newEntries, null);
    }

    /**
     * @param rtStubsPath
     *            the path to the RT stubs
     * @return a rt.jar (stubs only)
     * @throws CoreException
     */
    public static IPath[] findRtJar(final IPath rtStubsPath) throws CoreException {
        final File rtStubs = JavaTestPlugin.getDefault().getFileInPlugin(rtStubsPath);
        TestCase.assertNotNull(rtStubs);
        TestCase.assertTrue(rtStubs.exists());
        return new IPath[] {
                Path.fromOSString(rtStubs.getPath()),
                null,
                null
        };
    }

    private static void addNatureToProject(final IProject proj, final String natureId, final IProgressMonitor monitor)
            throws CoreException {
        final IProjectDescription description = proj.getDescription();
        final String[] prevNatures = description.getNatureIds();
        final String[] newNatures = new String[prevNatures.length + 1];
        System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
        newNatures[prevNatures.length] = natureId;
        description.setNatureIds(newNatures);
        proj.setDescription(description, monitor);
    }

    /**
     * Imports resources from <code>bundleSourcePath</code> inside
     * <code>bundle</code> into <code>importTarget</code>.
     * 
     * @param importTarget
     *            the parent container
     * @param bundle
     *            the bundle
     * @param bundleSourcePath
     *            the path to a folder containing resources
     * 
     * @throws CoreException
     *             import failed
     * @throws IOException
     *             import failed
     */
    public static void importResources(final IContainer importTarget, final Bundle bundle, final String bundleSourcePath)
            throws CoreException, IOException {
        final Enumeration entryPaths = bundle.getEntryPaths(bundleSourcePath);
        while (entryPaths.hasMoreElements()) {
            final String path = (String) entryPaths.nextElement();
            final IPath name = new Path(path.substring(bundleSourcePath.length()));
            if (path.endsWith("/")) {
                final IFolder folder = importTarget.getFolder(name);
                folder.create(false, true, null);
                importResources(folder, bundle, path);
            } else {
                final URL url = bundle.getEntry(path);
                final IFile file = importTarget.getFile(name);
                file.create(url.openStream(), true, null);
            }
        }
    }

    private static class ImportOverwriteQuery implements IOverwriteQuery {
        @Override
        public String queryOverwrite(final String file) {
            return ALL;
        }
    }

    private static class Requestor extends TypeNameRequestor {
    }

    public static void emptyDisplayLoop() {
        final boolean showDebugInfo = false;

        final Display display = Display.getCurrent();
        if (display != null) {
            if (showDebugInfo) {
                try {
                    final Synchronizer synchronizer = display.getSynchronizer();
                    final Field field = Synchronizer.class.getDeclaredField("messageCount");
                    field.setAccessible(true);
                    System.out.println("Processing " + field.getInt(synchronizer) + " messages in queue");
                } catch (final Exception e) {
                    // ignore
                    System.out.println(e);
                }
            }
            while (display.readAndDispatch()) { /* loop */
            }
        }
    }
}
