package pl.greenpath.mockito.ide.refactoring.proposal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import pl.greenpath.mockito.ide.refactoring.ast.ContextBaseTypeFinder;
import pl.greenpath.mockito.ide.refactoring.proposal.strategy.MockProposalStrategy;
import pl.greenpath.mockito.ide.refactoring.proposal.strategy.ProposalStrategy;
import pl.greenpath.mockito.ide.refactoring.quickfix.exception.NotSupportedRefactoring;

@RunWith(MockitoJUnitRunner.class)
public class ToLocalMockConverterTest {

    private static final String MOCK_STATIC_IMPORT = "org.mockito.Mockito.mock";

    @Mock
    private ICompilationUnit cu;
    private final AST ast = AST.newAST(AST.JLS4);
    private ImportRewrite importRewrite;

    @Before
    public void before() throws JavaModelException {
        importRewrite = ImportRewrite.create(cu, false);
    }

    @Test
    public void shouldConvertToLocalMock() throws NotSupportedRefactoring {
        final SimpleName selectedNode = ast.newSimpleName("testName");
        final ExpressionStatement result = checkConversion(new MockProposalStrategy(selectedNode), selectedNode);

        assertThat(result.toString()).isEqualTo("TestedClass testName=mock(TestedClass.class);\n");
        assertThat(importRewrite.getAddedStaticImports()).contains(MOCK_STATIC_IMPORT).describedAs(
                "Should add only %s, added: %s", MOCK_STATIC_IMPORT,
                Arrays.toString(importRewrite.getAddedStaticImports()));

    }

    @SuppressWarnings("rawtypes")
    public ExpressionStatement checkConversion(final ProposalStrategy proposalStrategy, final SimpleName selectedNode)
            throws NotSupportedRefactoring {
        final MethodDeclaration method = getMethodDeclaration("test", getVariableDeclaration(selectedNode));

        final ToLocalMockConverter testedClass = new ToLocalMockConverter(importRewrite, selectedNode,
                proposalStrategy, getFinderMock());

        final List rewrittenStatements = testedClass.performFix()
                .getListRewrite(method.getBody(), Block.STATEMENTS_PROPERTY).getRewrittenList();
        final ExpressionStatement mockDeclaration = (ExpressionStatement) rewrittenStatements.get(0);
        final VariableDeclarationStatement variableDeclaration = (VariableDeclarationStatement) rewrittenStatements
                .get(1);

        assertThat(variableDeclaration.toString()).isEqualTo("TestedClass type=testName;\n");

        return mockDeclaration;
    }

    private ContextBaseTypeFinder getFinderMock() throws NotSupportedRefactoring {
        final ContextBaseTypeFinder finderMock = mock(ContextBaseTypeFinder.class);
        final ITypeBinding typeBindingMock = mock(ITypeBinding.class);
        final ITypeBinding typeDeclarationMock = mock(ITypeBinding.class);
        when(typeDeclarationMock.getQualifiedName()).thenReturn("pl.greenpath.test.TestedClass");
        when(typeBindingMock.getTypeDeclaration()).thenReturn(typeDeclarationMock);
        when(typeBindingMock.getName()).thenReturn("TestedClass");
        when(typeBindingMock.getTypeArguments()).thenReturn(new ITypeBinding[0]);
        when(finderMock.find((ASTNode) Mockito.any())).thenReturn(typeBindingMock);
        return finderMock;
    }

    private VariableDeclarationStatement getVariableDeclaration(final SimpleName newSimpleName) {
        final VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
        fragment.setName(ast.newSimpleName("type"));
        fragment.setInitializer(newSimpleName);
        final VariableDeclarationStatement statement = ast.newVariableDeclarationStatement(fragment);
        statement.setType(ast.newSimpleType(ast.newName("TestedClass")));
        return statement;
    }

    @SuppressWarnings("unchecked")
    private MethodDeclaration getMethodDeclaration(final String methodName,
            final VariableDeclarationStatement expressionStatement) {
        final MethodDeclaration result = ast.newMethodDeclaration();
        result.setName(ast.newSimpleName(methodName));
        result.setBody(ast.newBlock());
        result.getBody().statements().add(expressionStatement);
        return result;
    }

}
