package pl.greenpath.mockito.ide.refactoring.quickfix.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import pl.greenpath.mockito.ide.refactoring.TestUtils;
import pl.greenpath.mockito.ide.refactoring.proposal.ConvertToMockRecordProposal;
import pl.greenpath.mockito.ide.refactoring.proposal.strategy.WhenThenReturnRecordingStrategy;
import pl.greenpath.mockito.ide.refactoring.proposal.strategy.WhenThenThrowRecordingStrategy;

@RunWith(MockitoJUnitRunner.class)
public class ConvertToRecordingFixTest {

    @Mock
    private IInvocationContext context;

    @Mock
    private IProblemLocation problemLocation;

    @Mock
    private CompilationUnit astRoot;

    @Mock
    private ICompilationUnit cu;

    @Before
    public void before() {
        when(context.getASTRoot()).thenReturn(astRoot);
        when(context.getCompilationUnit()).thenReturn(cu);
    }

    @Test
    public void shouldBeApplicable_methodInvocation() {
        final ConvertToRecordingFix testedClass = new ConvertToRecordingFix(new WhenThenReturnRecordingStrategy());
        final ExpressionStatement methodInvocation = TestUtils.createMethodInvocationExpression("test", "equals",
                "something");
        when(context.getCoveringNode()).thenReturn(methodInvocation);
        when(problemLocation.getProblemId()).thenReturn(IProblem.ParsingErrorInsertToComplete);
        assertThat(testedClass.isApplicable(context, problemLocation)).isTrue();
    }

    @Test
    public void shouldNotBeApplicable_notMethodInvocation() {
        final ConvertToRecordingFix testedClass = new ConvertToRecordingFix(new WhenThenReturnRecordingStrategy());
        when(context.getCoveringNode()).thenReturn(TestUtils.AST_INSTANCE.newSimpleName("test"));
        when(problemLocation.getProblemId()).thenReturn(IProblem.ParsingErrorInsertToComplete);
        assertThat(testedClass.isApplicable(context, problemLocation)).isFalse();
    }

    @Test
    public void shouldNotBeApplicable_unsupportedExpressionStatement() {
        final ConvertToRecordingFix testedClass = new ConvertToRecordingFix(new WhenThenReturnRecordingStrategy());
        when(context.getCoveringNode()).thenReturn(
                TestUtils.AST_INSTANCE.newExpressionStatement(TestUtils.AST_INSTANCE.newBooleanLiteral(false)));
        when(problemLocation.getProblemId()).thenReturn(IProblem.ParsingErrorInsertToComplete);
        assertThat(testedClass.isApplicable(context, problemLocation)).isFalse();
    }

    @Test
    public void shouldNotBeApplicable_localMethodInvocation() {
        final ConvertToRecordingFix testedClass = new ConvertToRecordingFix(new WhenThenReturnRecordingStrategy());
        when(context.getCoveringNode()).thenReturn(TestUtils.createMethodInvocation("test", "Object"));
        when(problemLocation.getProblemId()).thenReturn(IProblem.ParsingErrorInsertToComplete);
        assertThat(testedClass.isApplicable(context, problemLocation)).isFalse();
    }

    @Test
    public void shouldReturnProposal_whenThenReturn() {
        final ConvertToRecordingFix testedClass = new ConvertToRecordingFix(new WhenThenReturnRecordingStrategy());
        final IJavaCompletionProposal result = testedClass.getProposal(context, problemLocation);
        assertThat(result).isInstanceOf(ConvertToMockRecordProposal.class);
        assertThat(result.getDisplayString()).isEqualTo("Convert to when(...).thenReturn(...)");
    }

    @Test
    public void shouldReturnProposal_whenThenThrow() {
        final ConvertToRecordingFix testedClass = new ConvertToRecordingFix(new WhenThenThrowRecordingStrategy());
        final IJavaCompletionProposal result = testedClass.getProposal(context, problemLocation);
        assertThat(result).isInstanceOf(ConvertToMockRecordProposal.class);
        assertThat(result.getDisplayString()).isEqualTo("Convert to when(...).thenThrow(...)");
    }

    @Test
    public void shouldOverrideToString() {
        final ConvertToRecordingFix testedClass = new ConvertToRecordingFix(new WhenThenThrowRecordingStrategy());
        assertThat(testedClass.toString()).isEqualTo(
                "ConvertToRecordingFix [recordingStrategy=WhenThenThrowRecordingStrategy]");
    }

}
