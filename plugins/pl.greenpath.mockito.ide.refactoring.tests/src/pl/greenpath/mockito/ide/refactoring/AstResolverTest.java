package pl.greenpath.mockito.ide.refactoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pl.greenpath.mockito.ide.refactoring.ast.AstResolver;

public class AstResolverTest {

    private static final String PROJECT_NAME = "test-project";
    private static ICompilationUnit _cu;
    private TypeDeclaration _type;
    private AstResolver testedClass;

    @BeforeClass
    public static void beforeClass() throws CoreException, InvocationTargetException, IOException, URISyntaxException {
        final String pluginPath = "/test/resources/test-project";
        final IJavaProject jproject = TestProjectHelper.importProject(pluginPath, PROJECT_NAME);

        final IPackageFragmentRoot sourceFolder = jproject.getPackageFragmentRoot(jproject.getResource().getProject()
                .getFolder("src"));
        _cu = sourceFolder.getPackageFragment("test1").getCompilationUnit("A.java");
    }

    @AfterClass
    public static void clearClass() throws CoreException {
        ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME).delete(true, new NullProgressMonitor());
    }

    @Before
    public void before() {
        final CompilationUnit astCu = ASTTesting.createAST(_cu);
        _type = (TypeDeclaration) astCu.types().get(0);
        testedClass = new AstResolver();
    }

    @Test
    public void shouldFindParentMethodDeclarationWhenGivenAstNode() {
        final ExpressionStatement invocationStatement = (ExpressionStatement) _type.getMethods()[0].getBody()
                .statements().get(0);
        final MethodInvocation bMethodInvocation = (MethodInvocation) invocationStatement.getExpression();
        final SimpleName testMockName = (SimpleName) bMethodInvocation.arguments().get(0);
        final MethodDeclaration result = testedClass.findParentOfType(testMockName, MethodDeclaration.class);
        assertEquals("a", result.getName().getIdentifier());
    }

    @Test
    public void shouldFindParentBodyDeclarationWhenGivenAstNode() {
        final MethodDeclaration aMethod = _type.getMethods()[0];
        final ExpressionStatement invocationStatement = (ExpressionStatement) aMethod.getBody().statements().get(0);
        final MethodInvocation bMethodInvocation = (MethodInvocation) invocationStatement.getExpression();
        final SimpleName testMockName = (SimpleName) bMethodInvocation.arguments().get(0);

        final BodyDeclaration result = testedClass.findParentOfType(testMockName, BodyDeclaration.class);
        assertEquals(aMethod, result);
    }

    @Test
    public void shouldReturnNullWhenNullGiven() {
        assertNull(testedClass.findParentOfType(null, BodyDeclaration.class));
    }

    @Test
    public void shouldReturnBodyDeclarationPropertyForAbstractTypeDeclaration() {
        final AbstractTypeDeclaration node = AST.newAST(AST.JLS4).newTypeDeclaration();
        final ChildListPropertyDescriptor result = new AstResolver().getBodyDeclarationsProperty(node);

        assertThat(result).isSameAs(node.getBodyDeclarationsProperty());
    }

    @Test
    public void shouldReturnBodyDeclarationPropertyForAnonymousTypeDeclaration() {
        final AnonymousClassDeclaration node = AST.newAST(AST.JLS4).newAnonymousClassDeclaration();
        final ChildListPropertyDescriptor result = new AstResolver().getBodyDeclarationsProperty(node);

        assertThat(result).isSameAs(AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY);
    }
}
