package pl.greenpath.mockito.ide.refactoring.quickfix.impl;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;

import pl.greenpath.mockito.ide.refactoring.ast.AstResolver;
import pl.greenpath.mockito.ide.refactoring.proposal.ConvertToMockRecordProposal;
import pl.greenpath.mockito.ide.refactoring.proposal.strategy.ConversionToRecordingStrategy;

public class ConvertToRecordingFix implements IQuickFix {

    private final ConversionToRecordingStrategy recordingStrategy;

    public ConvertToRecordingFix(final ConversionToRecordingStrategy whenThenReturnRecordingStrategy) {
        recordingStrategy = whenThenReturnRecordingStrategy;
    }

    @Override
    public boolean isApplicable(final IInvocationContext context, final IProblemLocation problemLocation) {
        final ExpressionStatement statement = getSelectedStatement(context);
        return problemLocation.getProblemId() == IProblem.ParsingErrorInsertToComplete
                && statement != null && statement.getExpression() instanceof MethodInvocation;
    }

    @Override
    public IJavaCompletionProposal getProposal(final IInvocationContext context, final IProblemLocation location) {
        return new ConvertToMockRecordProposal(context.getCompilationUnit(), getSelectedNode(context),
                context.getASTRoot(), recordingStrategy);
    }

    private ExpressionStatement getSelectedStatement(final IInvocationContext context) {
        return new AstResolver().findParentOfType(context.getCoveringNode(),
                ExpressionStatement.class);
    }

    private MethodInvocation getSelectedNode(final IInvocationContext context) {
        return new AstResolver().findParentOfType(context.getCoveringNode(), MethodInvocation.class);
    }

    @Override
    public String toString() {
        return "ConvertToRecordingFix [recordingStrategy=" + recordingStrategy.getClass().getSimpleName() + "]";
    }
}