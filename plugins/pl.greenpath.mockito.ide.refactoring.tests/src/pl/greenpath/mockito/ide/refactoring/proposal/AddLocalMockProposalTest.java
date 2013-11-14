package pl.greenpath.mockito.ide.refactoring.proposal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
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
import org.eclipse.jdt.testplugin.JavaProjectHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pl.greenpath.mockito.ide.refactoring.ASTTesting;
import pl.greenpath.mockito.ide.refactoring.quickfix.exception.NotSupportedRefactoring;

public class AddLocalMockProposalTest {

    private static IJavaProject _testProject;
    private static IPackageFragmentRoot _sourceFolder;
    private static ICompilationUnit _cu;
    private TypeDeclaration _type;
    private CompilationUnit _astCu;

    @BeforeClass
    public static void beforeClass() throws CoreException {
        _testProject = JavaProjectHelper.createJavaProject("testProject", "bin");
        _sourceFolder = JavaProjectHelper.addSourceContainer(_testProject, "src");
        JavaProjectHelper.addRTJar16(_testProject);
        _cu = createCompilationUnit();
    }

    @AfterClass
    public static void clearClass() throws CoreException {
        JavaProjectHelper.delete(_testProject);
    }

    @Before
    public void before() {
        _astCu = ASTTesting.createAST(_cu);
        _type = (TypeDeclaration) _astCu.types().get(0);
    }

    @Test
    public void shouldCreateMockOfSpecifiedType() throws NotSupportedRefactoring, CoreException {
        final MethodDeclaration aMethod = _type.getMethods()[0];
        final ExpressionStatement invocationStatement = (ExpressionStatement) aMethod.getBody().statements().get(0);
        final MethodInvocation bMethodInvocation = (MethodInvocation) invocationStatement.getExpression();
        final SimpleName selectedNode = (SimpleName) bMethodInvocation.arguments().get(0);
        final AddLocalMockProposal testedClass = new AddLocalMockProposal(_cu, selectedNode, _astCu);

        final List rewrittenList = testedClass.getRewrite().getListRewrite(aMethod.getBody(), Block.STATEMENTS_PROPERTY).getRewrittenList();
        final Object result = rewrittenList.get(0);
        final ExpressionStatement expressionStatement = (ExpressionStatement)result;
        final Assignment assignment = (Assignment) expressionStatement.getExpression();
        final VariableDeclarationExpression leftSide = (VariableDeclarationExpression)assignment.getLeftHandSide();
        final Type type = leftSide.getType();
        final MethodInvocation rightSide = (MethodInvocation) assignment.getRightHandSide();
        assertThat(((SimpleType)type).getName().getFullyQualifiedName()).isEqualTo("String");
        assertThat(((VariableDeclarationFragment)leftSide.fragments().get(0)).getName().getIdentifier()).isEqualTo("testMock");
        assertThat(rightSide.getName().getIdentifier()).isEqualTo("mock");
        assertThat(((SimpleType)((TypeLiteral)rightSide.arguments().get(0)).getType()).getName().getFullyQualifiedName()).isEqualTo("String");
    }
    
    @Test
    public void shouldReturnHighRelevanceWhenVariableEndsWithMock() {
        final MethodDeclaration aMethod = _type.getMethods()[0];
        final ExpressionStatement invocationStatement = (ExpressionStatement) aMethod.getBody().statements().get(0);
        final MethodInvocation bMethodInvocation = (MethodInvocation) invocationStatement.getExpression();
        final SimpleName selectedNode = (SimpleName) bMethodInvocation.arguments().get(0);
        
        final AddLocalMockProposal testedClass = new AddLocalMockProposal(_cu, selectedNode, _astCu);
        
        assertThat(testedClass.getRelevance()).isEqualTo(99);
    }

    @Test
    public void shouldReturnLowRelevanceWhenVariableDoesntEndsWithMock() {
        final MethodDeclaration aMethod = _type.getMethods()[7];
        final ExpressionStatement invocationStatement = (ExpressionStatement) aMethod.getBody().statements().get(0);
        final MethodInvocation bMethodInvocation = (MethodInvocation) invocationStatement.getExpression();
        final SimpleName selectedNode = (SimpleName) bMethodInvocation.arguments().get(0);
        
        final AddLocalMockProposal testedClass = new AddLocalMockProposal(_cu, selectedNode, _astCu);
        
        assertThat(testedClass.getRelevance()).isLessThan(90);
    }
    
    @Test
    public void imageShouldNotBeNull() {
        final MethodDeclaration aMethod = _type.getMethods()[7];
        final ExpressionStatement invocationStatement = (ExpressionStatement) aMethod.getBody().statements().get(0);
        final MethodInvocation bMethodInvocation = (MethodInvocation) invocationStatement.getExpression();
        final SimpleName selectedNode = (SimpleName) bMethodInvocation.arguments().get(0);
        
        final AddLocalMockProposal testedClass = new AddLocalMockProposal(_cu, selectedNode, _astCu);
        
        assertThat(testedClass.getImage()).isNotNull();
    }

    public static ICompilationUnit createCompilationUnit() throws CoreException, JavaModelException {
        final IPackageFragment packageFragment = _sourceFolder.createPackageFragment("test1", false, null);
        final StringBuilder buf = new StringBuilder();
        buf.append("package test1;\n");
        buf.append("import java.util.ArrayList;");
        buf.append("public class A {\n");
        buf.append("    public void a() { b(testMock);}\n");
        buf.append("    public void b(String a) { }\n");
        buf.append("    public void c() { Double[] d = new Double[] { test2Mock };  }\n");
        buf.append("    public void d() { ArrayList<String> s = new ArrayList<String>(test2Mock);  }\n");
        buf.append("    public void e() { int[] arr = new int[2]; arr[0] = test3Mock;  }\n");
        buf.append("    public String f() { return test4Mock;  }\n");
        buf.append("    public void g() { long t = test4Mock;  }\n");
        buf.append("    public void h() { b(test);  }\n");
        buf.append("}\n");
        return packageFragment.createCompilationUnit("A.java", buf.toString(), false, null);
    }
}
