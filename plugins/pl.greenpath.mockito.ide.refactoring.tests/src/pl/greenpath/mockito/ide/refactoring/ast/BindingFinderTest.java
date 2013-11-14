package pl.greenpath.mockito.ide.refactoring.ast;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.testplugin.JavaProjectHelper;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import pl.greenpath.mockito.ide.refactoring.ASTTesting;

public class BindingFinderTest {

    private static IJavaProject _testProject;
    private static IPackageFragmentRoot _sourceFolder;
    private static ICompilationUnit _cu;
    private TypeDeclaration _type;
    private AstResolver testedClass;
    private BindingFinder _testedClass;

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
        _testedClass = new BindingFinder();
    }

    @Test
    public void shouldFindAbstractTypeDeclaration() {
        final MethodDeclaration aMethod = _type.getMethods()[0];

        assertThat(_testedClass.getParentTypeBinding(aMethod)).isEqualTo(_type.resolveBinding());
    }

    @Test
    public void shouldFindAnonymousClassDeclaration() {
        final MethodDeclaration aMethod = _type.getMethods()[2];
        final ReturnStatement declaration = (ReturnStatement) aMethod.getBody().statements().get(0);
        final ClassInstanceCreation expression = (ClassInstanceCreation) declaration.getExpression();
        final AnonymousClassDeclaration anonymousClass = expression.getAnonymousClassDeclaration();
        final MethodDeclaration methodDeclaration = (MethodDeclaration) anonymousClass.bodyDeclarations().get(0);
        final Statement toStringStatement = (Statement) methodDeclaration.getBody().statements().get(0);

        assertThat(_testedClass.getParentTypeBinding(toStringStatement)).isEqualTo(anonymousClass.resolveBinding());
    }

    @Test
    public void shouldReturnNullForNullArgument() {
        assertThat(_testedClass.getParentTypeBinding(null)).isNull();
    }

    public static ICompilationUnit createCompilationUnit() throws CoreException, JavaModelException {
        final IPackageFragment packageFragment = _sourceFolder.createPackageFragment("test1", false, null);
        final String content = "package test1;\n"
                + "import java.util.ArrayList;\n"
                + "public class A {\n"
                + "    public void a() { b(testMock);}\n"
                + "    public void b(String a) { System.out.println(c()); }\n"
                + "     public Object c() { "
                + "          return new Object() { public String toString(){return test; }};"
                + "         }\n"
                + "}\n";
        return packageFragment.createCompilationUnit("A.java", content, false, null);
    }

}
