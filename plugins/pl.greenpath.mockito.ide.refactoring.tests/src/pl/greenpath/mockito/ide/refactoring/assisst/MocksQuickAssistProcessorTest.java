package pl.greenpath.mockito.ide.refactoring.assisst;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.junit.Test;

import pl.greenpath.mockito.ide.refactoring.proposal.ConvertToFieldMockProposal;

public class MocksQuickAssistProcessorTest {

    private final AST ast = AST.newAST(AST.JLS4);

    @Test
    public void test() throws CoreException {
        final MocksQuickAssistProcessor testedClass = new MocksQuickAssistProcessor();

        final IInvocationContext contextMock = mock(IInvocationContext.class);
        final ICompilationUnit cuMock = mock(ICompilationUnit.class);
        when(contextMock.getCompilationUnit()).thenReturn(cuMock);
        final CompilationUnit astMock = mock(CompilationUnit.class);
        when(contextMock.getASTRoot()).thenReturn(astMock);
        final ASTNode value = getMockVariableDeclaration();
        when(contextMock.getCoveringNode()).thenReturn(value);

        final IProblemLocation problem1 = mock(IProblemLocation.class);
        final IProblemLocation[] locations = new IProblemLocation[] { problem1 };
        final IJavaCompletionProposal[] result = testedClass.getAssists(contextMock, locations);
        assertThat(result).hasSize(1);
        assertThat(result[0]).isInstanceOf(ConvertToFieldMockProposal.class);
    }

    @SuppressWarnings("unchecked")
    private VariableDeclarationStatement getMockVariableDeclaration() {
        final MethodInvocation methodInvocation = getMethodInvocation("mock");
        final VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
        fragment.setInitializer(methodInvocation);
        fragment.setName(ast.newSimpleName("test"));
        final VariableDeclarationStatement result = ast.newVariableDeclarationStatement(fragment);

        final TypeDeclaration typeDeclaration = ast.newTypeDeclaration();
        final MethodDeclaration newMethodDeclaration = ast.newMethodDeclaration();
        newMethodDeclaration.setName(ast.newSimpleName("method"));
        newMethodDeclaration.setBody(ast.newBlock());
        newMethodDeclaration.getBody().statements().add(result);
        typeDeclaration.bodyDeclarations().add(newMethodDeclaration);
        return result;
    }

    private MethodInvocation getMethodInvocation(final String invokedMethod) {
        final MethodInvocation methodInvocation = ast.newMethodInvocation();
        methodInvocation.setName(ast.newSimpleName(invokedMethod));
        final TypeLiteral typeLiteral = ast.newTypeLiteral();
        typeLiteral.setType(ast.newSimpleType(ast.newName("Object")));
        methodInvocation.arguments().add(typeLiteral);
        return methodInvocation;
    }

}
