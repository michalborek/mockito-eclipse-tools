package pl.greenpath.mockito.ide.refactoring.ast;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
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
    public static void beforeClass() throws CoreException, InvocationTargetException, IOException {
        final String pluginPath = "/test/resources/test-project";
        final IJavaProject jproject = TestProjectHelper.importProject(pluginPath, PROJECT_NAME);
        final IPackageFragmentRoot sourceFolder = jproject.getPackageFragmentRoot(jproject.getResource().getProject()
                .getFolder("src"));
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

    @Test
    public void shouldDelegateResolveBindingToType() {
        final MethodDeclaration aMethod = _type.getMethods()[1];
        final SingleVariableDeclaration argument = (SingleVariableDeclaration) aMethod.parameters().get(0);
        
        final ITypeBinding result = _testedClass.resolveBinding(argument.getType());
        assertThat(result.getKey()).isEqualTo("Ljava/lang/String;");
    }

    @Test
    public void shouldDelegateResolveBindingToSimpleType() {
        final ExpressionStatement invocationStatement = (ExpressionStatement) _type.getMethods()[0].getBody()
                .statements().get(0);
        final SimpleName simpleName = ((MethodInvocation) invocationStatement.getExpression()).getName();
        final IBinding result = _testedClass.resolveBinding(simpleName);
        assertThat(result.getKey()).isEqualTo("Ltest1/B;.b(Ljava/lang/String;)V");

    }
}
