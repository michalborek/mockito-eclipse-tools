package pl.greenpath.mockito.ide.refactoring.proposal;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.zip.ZipException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pl.greenpath.mockito.ide.refactoring.ASTTesting;
import pl.greenpath.mockito.ide.refactoring.TestProjectHelper;
import pl.greenpath.mockito.ide.refactoring.ast.ContextBaseTypeFinder;

public class ContextBaseTypeFinderTest {

    private static final String PROJECT_NAME = "test-project";

    private static ICompilationUnit _cu;
    private TypeDeclaration _type;

    @BeforeClass
    public static void beforeClass() throws CoreException, InvocationTargetException, ZipException, IOException {
        final String pluginPath = "/test/resources/test-project";
        final IJavaProject jproject = TestProjectHelper.importProject(pluginPath, PROJECT_NAME);
        final IPackageFragmentRoot sourceFolder = jproject.getPackageFragmentRoot(jproject.getResource().getProject()
                .getFolder("src"));
        _cu = sourceFolder.getPackageFragment("test1").getCompilationUnit("D.java");
    }

    @AfterClass
    public static void clearClass() throws CoreException {
        ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME).delete(true, new NullProgressMonitor());
    }

    @Before
    public void before() {
        final CompilationUnit astCu = ASTTesting.createAST(_cu);
        _type = (TypeDeclaration) astCu.types().get(0);
    }

    @Test
    public void shouldFindTypeOfMethodInvocationParameter() {
        final MethodDeclaration aMethod = _type.getMethods()[0];
        final ExpressionStatement invocationStatement = (ExpressionStatement) aMethod.getBody().statements().get(0);
        final MethodInvocation bMethodInvocation = (MethodInvocation) invocationStatement.getExpression();
        final SimpleName testMockName = (SimpleName) bMethodInvocation.arguments().get(0);

        final ContextBaseTypeFinder finder = new ContextBaseTypeFinder();

        assertEquals("java.lang.String", finder.find(testMockName).getQualifiedName());
    }

    @Test
    public void shouldFindTypeOfArrayInitializerParameter() {
        final MethodDeclaration cMethod = _type.getMethods()[2];
        final VariableDeclarationStatement methodInvocationStatement = (VariableDeclarationStatement) cMethod.getBody()
                .statements().get(0);
        final VariableDeclarationFragment fragment = (VariableDeclarationFragment) methodInvocationStatement
                .fragments().get(0);

        final ArrayCreation initializer = (ArrayCreation) fragment.getInitializer();
        final SimpleName testMockName = (SimpleName) initializer.getInitializer().expressions().get(0);

        final ContextBaseTypeFinder finder = new ContextBaseTypeFinder();

        assertEquals("java.lang.Double", finder.find(testMockName).getQualifiedName());
    }

    @Test
    public void shouldFindTypeOfConstructorInvocationParameter() {
        final MethodDeclaration dMethod = _type.getMethods()[3];
        final VariableDeclarationStatement invocationStatement = (VariableDeclarationStatement) dMethod.getBody()
                .statements().get(0);
        final VariableDeclarationFragment fragment = (VariableDeclarationFragment) invocationStatement
                .fragments().get(0);

        final ClassInstanceCreation initializer = (ClassInstanceCreation) fragment.getInitializer();
        final SimpleName testMockName = (SimpleName) initializer.arguments().get(0);

        final ContextBaseTypeFinder finder = new ContextBaseTypeFinder();

        assertEquals("java.util.Collection<? extends java.lang.String>", finder.find(testMockName).getQualifiedName());
    }

    @Test
    public void shouldFindTypeOfAssignment() {
        final MethodDeclaration eMethod = _type.getMethods()[4];
        final ExpressionStatement expression = (ExpressionStatement) eMethod.getBody().statements().get(1);
        final Assignment assignment = (Assignment) expression.getExpression();
        final SimpleName testMockName = (SimpleName) assignment.getRightHandSide();

        final ContextBaseTypeFinder finder = new ContextBaseTypeFinder();

        assertEquals("int", finder.find(testMockName).getQualifiedName());
    }

    @Test
    public void shouldFindTypeOfReturnStatement() {
        final MethodDeclaration fMethod = _type.getMethods()[5];
        final ReturnStatement expression = (ReturnStatement) fMethod.getBody().statements().get(0);
        final SimpleName testMockName = (SimpleName) expression.getExpression();

        final ContextBaseTypeFinder finder = new ContextBaseTypeFinder();

        assertEquals("java.lang.String", finder.find(testMockName).getQualifiedName());
    }

    @Test
    public void shouldFindTypeOfVariableDeclaration() {
        final MethodDeclaration gMethod = _type.getMethods()[6];
        final VariableDeclarationStatement methodInvocationStatement = (VariableDeclarationStatement) gMethod.getBody()
                .statements().get(0);
        final VariableDeclarationFragment fragment = (VariableDeclarationFragment) methodInvocationStatement
                .fragments().get(0);

        final SimpleName testMockName = (SimpleName) fragment.getInitializer();
        final ContextBaseTypeFinder finder = new ContextBaseTypeFinder();

        assertEquals("long", finder.find(testMockName).getQualifiedName());
    }
}
