package pl.greenpath.mockito.ide.refactoring.assisst;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import pl.greenpath.mockito.ide.refactoring.proposal.ConvertToFieldMockProposal;

@RunWith(MockitoJUnitRunner.class)
public class MocksQuickAssistProcessorTest {

    @Mock
    private CompilationUnit astRootMock;

    @Mock
    private ICompilationUnit cuMock;

    @Mock
    private IInvocationContext contextMock;

    private final AST ast = AST.newAST(AST.JLS4);

    private IProblemLocation[] locations;

    private MocksQuickAssistProcessor testedClass;

    @Before
    public void before() {
        when(contextMock.getCompilationUnit()).thenReturn(cuMock);
        when(contextMock.getASTRoot()).thenReturn(astRootMock);
        locations = new IProblemLocation[] { mock(IProblemLocation.class) };
        testedClass = new MocksQuickAssistProcessor();
    }

    @Test
    public void shouldReturnConvertToFieldProposalForLocalMockDeclaration() throws CoreException {
        final VariableDeclarationFragment variableDeclaration = createVariableDeclaration("Object", "type");
        variableDeclaration.setInitializer(getMethodInvocation("mock", "Object"));

        when(contextMock.getCoveringNode()).thenReturn(getVariableDeclarationStatement(variableDeclaration));

        final IJavaCompletionProposal[] result = testedClass.getAssists(contextMock, locations);

        assertThat(result).hasSize(1);
        assertThat(result[0]).isInstanceOf(ConvertToFieldMockProposal.class);
    }

    @Test
    public void shouldNotReturnConvertToFieldProposalWhenConflictingWithFieldName() throws CoreException {
        final VariableDeclarationFragment variableDeclaration = createVariableDeclaration("Object", "conflicting");
        variableDeclaration.setInitializer(getMethodInvocation("mock", "Object"));

        when(contextMock.getCoveringNode()).thenReturn(getVariableDeclarationStatement(variableDeclaration));

        assertThat(testedClass.getAssists(contextMock, locations)).isEmpty();
    }

    @Test
    public void shouldNotReturnConvertToFieldProposalIsNotLocalMock() throws CoreException {
        final VariableDeclarationFragment variableDeclaration = createVariableDeclaration("Object", "type");
        variableDeclaration.setInitializer(getMethodInvocation("notAMock", "Object"));

        when(contextMock.getCoveringNode()).thenReturn(getVariableDeclarationStatement(variableDeclaration));

        final IJavaCompletionProposal[] result = testedClass.getAssists(contextMock, locations);

        assertThat(result).isEmpty();
    }

    @Test
    public void shouldNotReturnConvertToFieldProposalIfNotMethodInvocationPresent() throws CoreException {
        final VariableDeclarationFragment variableDeclaration = createVariableDeclaration("Object", "type");
        variableDeclaration.setInitializer(ast.newStringLiteral());

        when(contextMock.getCoveringNode()).thenReturn(getVariableDeclarationStatement(variableDeclaration));

        final IJavaCompletionProposal[] result = testedClass.getAssists(contextMock, locations);

        assertThat(result).isEmpty();
    }

    @Test
    public void shouldReturnEmptyArrayWhenCannotAnyUseConverter() throws CoreException {
        when(contextMock.getCoveringNode()).thenReturn(ast.newSimpleName("testName"));

        assertThat(testedClass.getAssists(contextMock, locations)).isEmpty();
    }

    private VariableDeclarationStatement getVariableDeclarationStatement(
            final VariableDeclarationFragment variableDeclaration) {
        final VariableDeclarationStatement declaration = ast.newVariableDeclarationStatement(variableDeclaration);
        createTypeStub(declaration);
        return declaration;
    }

    private void createTypeStub(final VariableDeclarationStatement mockDeclaration) {
        final FieldDeclaration fieldDeclaration = ast.newFieldDeclaration(createVariableDeclaration("Object",
                "conflicting"));
        final MethodDeclaration methodDeclaration = createMethodDeclaration(mockDeclaration);
        createTypeDeclaration(fieldDeclaration, methodDeclaration);
    }

    private VariableDeclarationFragment createVariableDeclaration(final String type, final String variableName) {
        final VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
        fragment.setName(ast.newSimpleName(variableName));
        return fragment;
    }

    @SuppressWarnings("unchecked")
    private TypeDeclaration createTypeDeclaration(final BodyDeclaration... bodyDeclarations) {
        final TypeDeclaration typeDeclaration = ast.newTypeDeclaration();

        for (final BodyDeclaration bodyDeclaration : bodyDeclarations) {
            typeDeclaration.bodyDeclarations().add(bodyDeclaration);
        }
        return typeDeclaration;
    }

    @SuppressWarnings("unchecked")
    private MethodDeclaration createMethodDeclaration(final VariableDeclarationStatement statement) {
        final MethodDeclaration newMethodDeclaration = ast.newMethodDeclaration();
        newMethodDeclaration.setName(ast.newSimpleName("method"));
        newMethodDeclaration.setBody(ast.newBlock());
        newMethodDeclaration.getBody().statements().add(statement);
        return newMethodDeclaration;
    }

    @SuppressWarnings("unchecked")
    private MethodInvocation getMethodInvocation(final String methodName, final String className) {
        final MethodInvocation methodInvocation = ast.newMethodInvocation();
        final SimpleType methodArgument = ast.newSimpleType(ast.newName(className));

        methodInvocation.setName(ast.newSimpleName(methodName));
        final TypeLiteral typeLiteral = ast.newTypeLiteral();
        typeLiteral.setType(methodArgument);
        methodInvocation.arguments().add(typeLiteral);
        return methodInvocation;
    }

}
