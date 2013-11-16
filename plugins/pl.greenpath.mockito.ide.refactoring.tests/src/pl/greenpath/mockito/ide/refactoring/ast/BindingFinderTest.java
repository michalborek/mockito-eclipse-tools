package pl.greenpath.mockito.ide.refactoring.ast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.ZipException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pl.greenpath.mockito.ide.refactoring.ASTTesting;
import pl.greenpath.mockito.ide.refactoring.TestProjectHelper;

public class BindingFinderTest {

    private static final String PROJECT_NAME = "test-project";
    
    private static ICompilationUnit _cu;
    private TypeDeclaration _type;
    private BindingFinder _testedClass;

    @BeforeClass
    public static void beforeClass() throws CoreException, InvocationTargetException, ZipException, IOException {
        final String pluginPath = "test/resources/test-project.zip";
        final IJavaProject jproject = TestProjectHelper.importProject(pluginPath, PROJECT_NAME);
        final IPackageFragmentRoot sourceFolder = jproject.getPackageFragmentRoot(jproject.getResource().getProject().getFolder("src"));
        _cu = sourceFolder.getPackageFragment("test1").getCompilationUnit("B.java");
    }

    @AfterClass
    public static void clearClass() throws CoreException {
        ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME).delete(true, new NullProgressMonitor());
    }


    @Before
    public void before() {
        final CompilationUnit astCu = ASTTesting.createAST(_cu);
        _type = (TypeDeclaration) astCu.types().get(0);
        _testedClass = new BindingFinder();
    }

    @Test
    public void shouldFindAbstractTypeDeclaration() {
        final MethodDeclaration aMethod = _type.getMethods()[0];

        assertEquals(_type.resolveBinding(), _testedClass.getParentTypeBinding(aMethod));
    }

    @Test
    public void shouldFindAnonymousClassDeclaration() {
        final MethodDeclaration aMethod = _type.getMethods()[2];
        final ReturnStatement declaration = (ReturnStatement) aMethod.getBody().statements().get(0);
        final ClassInstanceCreation expression = (ClassInstanceCreation) declaration.getExpression();
        final AnonymousClassDeclaration anonymousClass = expression.getAnonymousClassDeclaration();
        final MethodDeclaration methodDeclaration = (MethodDeclaration) anonymousClass.bodyDeclarations().get(0);
        final Statement toStringStatement = (Statement) methodDeclaration.getBody().statements().get(0);

        assertEquals(anonymousClass.resolveBinding(), _testedClass.getParentTypeBinding(toStringStatement));
    }

    @Test
    public void shouldReturnNullForNullArgument() {
        assertNull(_testedClass.getParentTypeBinding(null));
    }

}
