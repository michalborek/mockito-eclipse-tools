package pl.greenpath.mockito.ide.refactoring;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.FileSystemStructureProvider;
import org.eclipse.ui.wizards.datatransfer.IImportStructureProvider;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;

// TODO refactor this
public class TestProjectHelper {

    public static IJavaProject importProject(final String projectPath, final String projectName)
            throws InvocationTargetException, CoreException, IOException {
        importFromZip(getFile(new Path(projectPath)), new Path(projectName), new NullProgressMonitor());

        final IProject testProject = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        testProject.open(new NullProgressMonitor());
        final IJavaProject jproject = JavaCore.create(testProject);
        if (!testProject.hasNature(JavaCore.NATURE_ID)) {
            addNatureToProject(testProject);
        }
        jproject.open(null);
        return jproject;
    }

    public static void importFromZip(final File file, final IPath destination, final IProgressMonitor monitor)
            throws InvocationTargetException {
        final IImportStructureProvider structureProvider = FileSystemStructureProvider.INSTANCE;

        final IOverwriteQuery overwriteQuery = new IOverwriteQuery() {
            @Override
            public String queryOverwrite(final String file) {
                return ALL;
            }
        };
        try {
            final ImportOperation op = new ImportOperation(destination, file, structureProvider, overwriteQuery);
            op.setCreateContainerStructure(false);
            op.run(monitor);
        } catch (final InterruptedException e) {
            // should not happen
        }
    }


    public static File getFile(final IPath path) throws IOException {
        final URL url = FileLocator.resolve(TestProjectHelper.class.getResource(path.toString()));
        return new File(url.getFile());
    }

    public static void addNatureToProject(final IProject project) throws CoreException {
        final IProjectDescription description = project.getDescription();
        description.setNatureIds(new String[] { JavaCore.NATURE_ID });
        project.setDescription(description, null);
    }
}
