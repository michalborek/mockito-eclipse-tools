package pl.greenpath.mockito.ide.refactoring.proposal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static pl.greenpath.mockito.ide.refactoring.ConversionsChecker.checkFieldMockWithExtraInterfaces;
import static pl.greenpath.mockito.ide.refactoring.ConversionsChecker.checkIfFieldMockHasBeenAdded;
import static pl.greenpath.mockito.ide.refactoring.ConversionsChecker.checkIfRunWithAnnotationIsProperlyAdded;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import pl.greenpath.mockito.ide.refactoring.TestUtils;
import pl.greenpath.mockito.ide.refactoring.ast.AstResolver;
import pl.greenpath.mockito.ide.refactoring.ast.BindingFinder;

@RunWith(MockitoJUnitRunner.class)
public class ConvertToFieldMockProposalTest {

    @Mock
    private ICompilationUnit cu;

    @Mock
    private BindingFinder bindingFinderMock;

    private CompilationUnit astRoot;

    @Before
    public void before() {
        astRoot = spy(TestUtils.AST_INSTANCE.newCompilationUnit());
        when(astRoot.getTypeRoot()).thenReturn(cu);
        final ITypeBinding typeBindingMock = getTypeBindingMock();
        when(bindingFinderMock.resolveBinding(any(Type.class))).thenReturn(typeBindingMock);
    }

    private ITypeBinding getTypeBindingMock() {
        final ITypeBinding typeBindingMock = mock(ITypeBinding.class);
        final ITypeBinding typeDeclarationMock = mock(ITypeBinding.class);
        when(typeDeclarationMock.getQualifiedName()).thenReturn("pl.greenpath.test.FooBar");
        when(typeBindingMock.getTypeDeclaration()).thenReturn(typeDeclarationMock);
        when(typeBindingMock.getName()).thenReturn("FooBar");
        when(typeBindingMock.getTypeArguments()).thenReturn(new ITypeBinding[0]);
        when(typeBindingMock.getAnnotations()).thenReturn(new IAnnotationBinding[0]);
        return typeBindingMock;
    }

    @Test
    public void shouldOverrideToString() {
        final VariableDeclarationStatement selectedNode = TestUtils.putVariableIntoStubStatement(TestUtils
                .createVariableDeclaration("test"));
        final ConvertToFieldMockProposal testedClass = new ConvertToFieldMockProposal(cu, selectedNode, astRoot);
        assertThat(testedClass.toString()).isEqualTo(
                "ConvertToFieldMockProposal [selectedStatement=\"Object test;\n\"]");
    }

    @Test
    public void shouldReturnNonNullImage() {
        final VariableDeclarationStatement selectedNode = TestUtils.putVariableIntoStubStatement(TestUtils
                .createVariableDeclaration("test"));
        final ConvertToFieldMockProposal testedClass = new ConvertToFieldMockProposal(cu, selectedNode, astRoot);
        assertThat(testedClass.getImage()).isNotNull();
    }

    @Test
    public void shouldConvertLocalMockToFieldMock() throws CoreException {
        final VariableDeclarationFragment variableDeclaration = TestUtils.createVariableDeclaration("fooMock");
        variableDeclaration.setInitializer(TestUtils.createMethodInvocation("mock", "Object"));
        final VariableDeclarationStatement statement = TestUtils.putVariableIntoStubStatement(variableDeclaration);
        final ConvertToFieldMockProposal testedClass = new ConvertToFieldMockProposal(cu, statement, astRoot,
                bindingFinderMock);

        final ASTRewrite rewrite = testedClass.getRewrite();
        final TypeDeclaration typeDeclaration = new AstResolver().findParentOfType(statement, TypeDeclaration.class);
        checkIfFieldMockHasBeenAdded(rewrite, typeDeclaration, "fooMock", "FooBar");
        checkIfRunWithAnnotationIsProperlyAdded(rewrite, typeDeclaration);
    }

    @Test
    public void shouldConvertToFieldWithExtraInterfacesMock() throws CoreException {
        final VariableDeclarationFragment variableDeclaration = getLocalMockDeclarationWithAdditionalArgument(
                "extraInterfaces", "List");
        final VariableDeclarationStatement statement = TestUtils.putVariableIntoStubStatement(variableDeclaration);
        final ConvertToFieldMockProposal testedClass = new ConvertToFieldMockProposal(cu, statement, astRoot,
                bindingFinderMock);

        final ASTRewrite rewrite = testedClass.getRewrite();
        final TypeDeclaration typeDeclaration = new AstResolver().findParentOfType(statement, TypeDeclaration.class);
        checkFieldMockWithExtraInterfaces(rewrite, typeDeclaration, "fooMock", "FooBar", "List");
        checkIfRunWithAnnotationIsProperlyAdded(rewrite, typeDeclaration);
    }

    @Test
    public void shouldConvertToFieldWithoutExtraInterfacesMock_wrongMethodInvoked() throws CoreException {
        final VariableDeclarationFragment variableDeclaration = getLocalMockDeclarationWithAdditionalArgument(
                "not_extraInterfaces", "List");
        final VariableDeclarationStatement statement = TestUtils.putVariableIntoStubStatement(variableDeclaration);
        final ConvertToFieldMockProposal testedClass = new ConvertToFieldMockProposal(cu, statement, astRoot,
                bindingFinderMock);

        final ASTRewrite rewrite = testedClass.getRewrite();
        final TypeDeclaration typeDeclaration = new AstResolver().findParentOfType(statement, TypeDeclaration.class);
        checkIfFieldMockHasBeenAdded(rewrite, typeDeclaration, "fooMock", "FooBar");
        checkIfRunWithAnnotationIsProperlyAdded(rewrite, typeDeclaration);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldConvertToFieldWithoutExtraInterfacesMock_secondArgumentNotAMethod() throws CoreException {
        final VariableDeclarationFragment variableDeclaration = TestUtils.createVariableDeclaration("fooMock");
        final MethodInvocation methodInvocation = TestUtils.createMethodInvocation("mock", "Object");
        final StringLiteral name = TestUtils.AST_INSTANCE.newStringLiteral();
        name.setLiteralValue("someName");
        methodInvocation.arguments().add(name);
        
        variableDeclaration.setInitializer(methodInvocation);
        final VariableDeclarationStatement statement = TestUtils.putVariableIntoStubStatement(variableDeclaration);
        final ConvertToFieldMockProposal testedClass = new ConvertToFieldMockProposal(cu, statement, astRoot,
                bindingFinderMock);

        final ASTRewrite rewrite = testedClass.getRewrite();
        final TypeDeclaration typeDeclaration = new AstResolver().findParentOfType(statement, TypeDeclaration.class);
        checkIfFieldMockHasBeenAdded(rewrite, typeDeclaration, "fooMock", "FooBar");
        checkIfRunWithAnnotationIsProperlyAdded(rewrite, typeDeclaration);
    }

    @SuppressWarnings("unchecked")
    private VariableDeclarationFragment getLocalMockDeclarationWithAdditionalArgument(final String argumentMethod,
            final String typeLiteral) {
        final VariableDeclarationFragment variableDeclaration = TestUtils.createVariableDeclaration("fooMock");
        final MethodInvocation methodInvocation = TestUtils.createMethodInvocation("mock", "Object");
        final MethodInvocation extraInterfaces = TestUtils.createMethodInvocation(argumentMethod, typeLiteral);
        final MethodInvocation withSettings = TestUtils.AST_INSTANCE.newMethodInvocation();
        withSettings.setName(TestUtils.AST_INSTANCE.newSimpleName("withSettings"));
        extraInterfaces.setExpression(withSettings);
        methodInvocation.arguments().add(extraInterfaces);
        variableDeclaration.setInitializer(methodInvocation);
        return variableDeclaration;
    }
}
