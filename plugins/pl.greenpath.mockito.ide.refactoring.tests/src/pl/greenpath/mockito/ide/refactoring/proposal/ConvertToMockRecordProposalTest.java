package pl.greenpath.mockito.ide.refactoring.proposal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import pl.greenpath.mockito.ide.refactoring.TestUtils;
import pl.greenpath.mockito.ide.refactoring.proposal.strategy.ConversionToRecordingStrategy;
import pl.greenpath.mockito.ide.refactoring.proposal.strategy.WhenThenReturnRecordingStrategy;

@RunWith(MockitoJUnitRunner.class)
// TODO tests for putting cursor into thenReturn(..)
public class ConvertToMockRecordProposalTest {

    @Mock
    private ICompilationUnit cu;
    private CompilationUnit astRoot;

    @Before
    public void before() {
        astRoot = spy(TestUtils.AST_INSTANCE.newCompilationUnit());
        when(astRoot.getTypeRoot()).thenReturn(cu);
    }

    @Test
    public void shouldReturnHighRelevance() {
        final ASTNode selectedNode = TestUtils.createVariableDeclaration("foo");
        final ConversionToRecordingStrategy strategy = new WhenThenReturnRecordingStrategy();
        final ConvertToMockRecordProposal testedClass = new ConvertToMockRecordProposal(cu, selectedNode, astRoot,
                strategy);
        assertThat(testedClass.getRelevance()).isEqualTo(99);

    }
    
    @Test
    public void shouldHaveProperDisplayName() {
        final ASTNode selectedNode = TestUtils.createVariableDeclaration("foo");
        final ConversionToRecordingStrategy strategy = new WhenThenReturnRecordingStrategy();
        final ConvertToMockRecordProposal testedClass = new ConvertToMockRecordProposal(cu, selectedNode, astRoot,
                strategy);
        assertThat(testedClass.getDisplayString()).isEqualTo("Convert to when(...).thenReturn(...)");
    }

    @Test
    public void shouldReturnImage() {
        final ASTNode selectedNode = TestUtils.createVariableDeclaration("foo");
        final ConversionToRecordingStrategy strategy = new WhenThenReturnRecordingStrategy();
        final ConvertToMockRecordProposal testedClass = new ConvertToMockRecordProposal(cu, selectedNode, astRoot,
                strategy);
        assertThat(testedClass.getImage()).isNotNull();
    }

    @Test
    public void shouldConvertSelection() throws CoreException {
        checkConversion(new WhenThenReturnRecordingStrategy(), "when(type.toString()).thenReturn();\n",
                TestUtils.createMethodInvocationExpression("type", "toString"));
    }

    private void checkConversion(final ConversionToRecordingStrategy strategy, final String resultStatement,
            final ExpressionStatement selectedExpression) throws CoreException {
        final MethodDeclaration method = TestUtils.createMethodDeclaration("testMethod", selectedExpression);

        final ConvertToMockRecordProposal testedClass = new ConvertToMockRecordProposal(cu,
                selectedExpression.getExpression(), astRoot, strategy);

        final ASTRewrite afterConversion = testedClass.getRewrite();
        final ExpressionStatement resultExpression = (ExpressionStatement) afterConversion
                .getListRewrite(method.getBody(), Block.STATEMENTS_PROPERTY).getRewrittenList().get(0);
        assertThat(resultExpression.toString()).isEqualTo(resultStatement);
    }

}
