package pl.greenpath.mockito.ide.refactoring;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.eclipse.ui.wizards.datatransfer.ZipFileStructureProvider;

// TODO refactor this
public class TestProjectHelper {

    public static IJavaProject importProject(final String projectPath, final String projectName)
            throws InvocationTargetException,
            ZipException, IOException, CoreException {
        IProject testProject;
        importFromZip(
                new ZipFile(getFile(new Path(projectPath))), new Path(projectName), new NullProgressMonitor());

        testProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        testProject.open(new NullProgressMonitor());
        final IJavaProject jproject = JavaCore.create(testProject);
        if (!testProject.hasNature(JavaCore.NATURE_ID)) {
            addNatureToProject(testProject);
        }
        jproject.open(null);
        return jproject;
    }

    public static void importFromZip(final ZipFile srcZipFile, final IPath destination,
            final IProgressMonitor monitor) throws InvocationTargetException {
        final ZipFileStructureProvider structureProvider = new ZipFileStructureProvider(srcZipFile);
        try {
            final ImportOperation op = new ImportOperation(destination, structureProvider.getRoot(), structureProvider,
                    new ImportOverwriteQuery());
            op.run(monitor);
        } catch (final InterruptedException e) {
            // should not happen
        }
    }

    private static class ImportOverwriteQuery implements IOverwriteQuery {
        @Override
        public String queryOverwrite(final String file) {
            return ALL;
        }
    }

    public static File getFile(final IPath path) {
        return path.toFile().getAbsoluteFile();
    }

    public static void addNatureToProject(final IProject project) throws CoreException {
        final IProjectDescription description = project.getDescription();
        description.setNatureIds(new String[] { JavaCore.NATURE_ID });
        project.setDescription(description, null);
    }
}
