package pl.greenpath.mockito.ide.refactoring.proposal;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
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
import org.eclipse.jdt.testplugin.JavaProjectHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pl.greenpath.mockito.ide.refactoring.ASTTesting;
import pl.greenpath.mockito.ide.refactoring.ast.ContextBaseTypeFinder;
import pl.greenpath.mockito.ide.refactoring.quickfix.exception.NotSupportedRefactoring;

@SuppressWarnings("restriction")
public class ContextBaseTypeFinderTest {

    private static IJavaProject _testProject;
    private static IPackageFragmentRoot _sourceFolder;
    private static ICompilationUnit _cu;
    private TypeDeclaration _type;

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
    }

    @Test
    public void shouldFindTypeOfMethodInvocationParameter() throws CoreException, NotSupportedRefactoring {
        final MethodDeclaration aMethod = _type.getMethods()[0];
        final ExpressionStatement invocationStatement = (ExpressionStatement) aMethod.getBody().statements().get(0);
        final MethodInvocation bMethodInvocation = (MethodInvocation) invocationStatement.getExpression();
        final SimpleName testMockName = (SimpleName) bMethodInvocation.arguments().get(0);

        final ContextBaseTypeFinder finder = new ContextBaseTypeFinder(testMockName);

        assertEquals("java.lang.String", finder.find().getQualifiedName());
    }

    @Test
    public void shouldFindTypeOfArrayInitializerParameter() throws CoreException, NotSupportedRefactoring {
        final MethodDeclaration cMethod = _type.getMethods()[2];
        final VariableDeclarationStatement methodInvocationStatement = (VariableDeclarationStatement) cMethod.getBody()
                .statements().get(0);
        final VariableDeclarationFragment fragment = (VariableDeclarationFragment) methodInvocationStatement
                .fragments().get(0);

        final ArrayCreation initializer = (ArrayCreation) fragment.getInitializer();
        final SimpleName testMockName = (SimpleName) initializer.getInitializer().expressions().get(0);

        final ContextBaseTypeFinder finder = new ContextBaseTypeFinder(testMockName);

        assertEquals("java.lang.Double", finder.find().getQualifiedName());
    }

    @Test
    public void shouldFindTypeOfConstructorInvocationParameter() throws CoreException, NotSupportedRefactoring {
        final MethodDeclaration dMethod = _type.getMethods()[3];
        final VariableDeclarationStatement invocationStatement = (VariableDeclarationStatement) dMethod.getBody()
                .statements().get(0);
        final VariableDeclarationFragment fragment = (VariableDeclarationFragment) invocationStatement
                .fragments().get(0);
        
        final ClassInstanceCreation initializer = (ClassInstanceCreation) fragment.getInitializer();
        final SimpleName testMockName = (SimpleName) initializer.arguments().get(0);
        
        final ContextBaseTypeFinder finder = new ContextBaseTypeFinder(testMockName);
        
        assertEquals("java.util.Collection<? extends java.lang.String>", finder.find().getQualifiedName());
    }

    @Test
    public void shouldFindTypeOfAssignment() throws CoreException, NotSupportedRefactoring {
        final MethodDeclaration eMethod = _type.getMethods()[4];
        final ExpressionStatement expression = (ExpressionStatement) eMethod.getBody().statements().get(1);
        final Assignment assignment = (Assignment) expression.getExpression();
        final SimpleName testMockName = (SimpleName) assignment.getRightHandSide();
        
        final ContextBaseTypeFinder finder = new ContextBaseTypeFinder(testMockName);
        
        assertEquals("int", finder.find().getQualifiedName());
    }
 
    @Test
    public void shouldFindTypeOfReturnStatement() throws CoreException, NotSupportedRefactoring {
        final MethodDeclaration fMethod = _type.getMethods()[5];
        final ReturnStatement expression = (ReturnStatement) fMethod.getBody().statements().get(0);
        final SimpleName testMockName = (SimpleName) expression.getExpression();
        
        final ContextBaseTypeFinder finder = new ContextBaseTypeFinder(testMockName);
        
        assertEquals("java.lang.String", finder.find().getQualifiedName());
    }

    @Test
    public void shouldFindTypeOfVariableDeclaration() throws CoreException, NotSupportedRefactoring {
        final MethodDeclaration gMethod = _type.getMethods()[6];
        final VariableDeclarationStatement methodInvocationStatement = (VariableDeclarationStatement) gMethod.getBody()
                .statements().get(0);
        final VariableDeclarationFragment fragment = (VariableDeclarationFragment) methodInvocationStatement
                .fragments().get(0);

        final SimpleName testMockName = (SimpleName) fragment.getInitializer();
        final ContextBaseTypeFinder finder = new ContextBaseTypeFinder(testMockName);

        assertEquals("long", finder.find().getQualifiedName());
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
