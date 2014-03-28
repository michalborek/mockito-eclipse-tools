package pl.greenpath.mockito.ide.refactoring.proposal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.zip.ZipException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pl.greenpath.mockito.ide.refactoring.ASTTesting;
import pl.greenpath.mockito.ide.refactoring.TestProjectHelper;
import pl.greenpath.mockito.ide.refactoring.proposal.strategy.MockProposalStrategy;
import pl.greenpath.mockito.ide.refactoring.quickfix.exception.NotSupportedRefactoringException;

public class AddLocalMockProposalTest {
    
    private static final String PROJECT_NAME = "test-project";

    private static ICompilationUnit _cu;
    private TypeDeclaration _type;
    private CompilationUnit _astCu;

    @BeforeClass
    public static void beforeClass() throws CoreException, InvocationTargetException, ZipException, IOException {
        final String pluginPath = "test/resources/test-project.zip";
        final IJavaProject jproject = TestProjectHelper.importProject(pluginPath, PROJECT_NAME);
        final IPackageFragmentRoot sourceFolder = jproject.getPackageFragmentRoot(jproject.getResource().getProject().getFolder("src"));
        _cu = sourceFolder.getPackageFragment("test1").getCompilationUnit("C.java");
    }

    @AfterClass
    public static void clearClass() throws CoreException {
        ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME).delete(true, new NullProgressMonitor());
    }

    @Before
    public void before() {
        _astCu = ASTTesting.createAST(_cu);
        _type = (TypeDeclaration) _astCu.types().get(0);
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void shouldCreateMockOfSpecifiedType() throws NotSupportedRefactoringException, CoreException {
        final MethodDeclaration aMethod = _type.getMethods()[0];
        final ExpressionStatement invocationStatement = (ExpressionStatement) aMethod.getBody().statements().get(0);
        final MethodInvocation bMethodInvocation = (MethodInvocation) invocationStatement.getExpression();
        final SimpleName selectedNode = (SimpleName) bMethodInvocation.arguments().get(0);
        final AddLocalMockitoProposal testedClass = new AddLocalMockitoProposal(_cu, selectedNode, _astCu, new MockProposalStrategy(selectedNode));

        final List rewrittenList = testedClass.getRewrite().getListRewrite(aMethod.getBody(), Block.STATEMENTS_PROPERTY).getRewrittenList();
        final Object result = rewrittenList.get(0);
        final ExpressionStatement expressionStatement = (ExpressionStatement)result;
        final Assignment assignment = (Assignment) expressionStatement.getExpression();
        final VariableDeclarationExpression leftSide = (VariableDeclarationExpression)assignment.getLeftHandSide();
        final Type type = leftSide.getType();
        final MethodInvocation rightSide = (MethodInvocation) assignment.getRightHandSide();
        assertEquals("String", ((SimpleType)type).getName().getFullyQualifiedName());
        assertEquals("testMock", ((VariableDeclarationFragment)leftSide.fragments().get(0)).getName().getIdentifier());
        assertEquals("mock", rightSide.getName().getIdentifier());
        assertEquals("String", ((SimpleType)((TypeLiteral)rightSide.arguments().get(0)).getType()).getName().getFullyQualifiedName());
    }
    
    @Test
    public void shouldReturnHighRelevanceWhenVariableEndsWithMock() {
        final MethodDeclaration aMethod = _type.getMethods()[0];
        final ExpressionStatement invocationStatement = (ExpressionStatement) aMethod.getBody().statements().get(0);
        final MethodInvocation bMethodInvocation = (MethodInvocation) invocationStatement.getExpression();
        final SimpleName selectedNode = (SimpleName) bMethodInvocation.arguments().get(0);
        
        final AddLocalMockitoProposal testedClass = new AddLocalMockitoProposal(_cu, selectedNode, _astCu, new MockProposalStrategy(selectedNode));
        
        assertEquals(99, testedClass.getRelevance());
    }

    @Test
    public void shouldReturnLowRelevanceWhenVariableDoesntEndsWithMock() {
        final MethodDeclaration aMethod = _type.getMethods()[7];
        final ExpressionStatement invocationStatement = (ExpressionStatement) aMethod.getBody().statements().get(0);
        final MethodInvocation bMethodInvocation = (MethodInvocation) invocationStatement.getExpression();
        final SimpleName selectedNode = (SimpleName) bMethodInvocation.arguments().get(0);
        
        final AddLocalMockitoProposal testedClass = new AddLocalMockitoProposal(_cu, selectedNode, _astCu, new MockProposalStrategy(selectedNode));
        
        assertTrue(testedClass.getRelevance() < 90);
    }
    
    @Test
    public void imageShouldNotBeNull() {
        final MethodDeclaration aMethod = _type.getMethods()[7];
        final ExpressionStatement invocationStatement = (ExpressionStatement) aMethod.getBody().statements().get(0);
        final MethodInvocation bMethodInvocation = (MethodInvocation) invocationStatement.getExpression();
        final SimpleName selectedNode = (SimpleName) bMethodInvocation.arguments().get(0);
        
        final AddLocalMockitoProposal testedClass = new AddLocalMockitoProposal(_cu, selectedNode, _astCu, new MockProposalStrategy(selectedNode));
        
        assertNotNull(testedClass.getImage());
    }

}
