package pl.greenpath.mockito.ide.refactoring;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.BodyDeclaration;
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
    
    private static IJavaProject _testProject;
    private static IPackageFragmentRoot _sourceFolder;
    private static ICompilationUnit _cu;
    private TypeDeclaration _type;
    private AstResolver testedClass;

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
        final CompilationUnit astCu = ASTTesting.createAST(_cu);
        _type = (TypeDeclaration) astCu.types().get(0);
        testedClass = new AstResolver();
    }

	@Test
	public void shouldFindParentMethodDeclarationWhenGivenAstNode() {
	    final MethodDeclaration aMethod = _type.getMethods()[0];
        final ExpressionStatement invocationStatement = (ExpressionStatement) aMethod.getBody().statements().get(0);
        final MethodInvocation bMethodInvocation = (MethodInvocation) invocationStatement.getExpression();
        final SimpleName testMockName = (SimpleName) bMethodInvocation.arguments().get(0);
	    
        final MethodDeclaration result = testedClass.findParentOfType(testMockName, MethodDeclaration.class);
        assertEquals(aMethod, result);
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
        buf.append("}\n");
        return packageFragment.createCompilationUnit("A.java", buf.toString(), false, null);
    }
}
