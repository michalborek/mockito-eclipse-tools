package pl.greenpath.mockito.ide.refactoring.quickfix.impl;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;

import pl.greenpath.mockito.ide.refactoring.ast.AstResolver;
import pl.greenpath.mockito.ide.refactoring.proposal.ConvertToMockRecordProposal;
import pl.greenpath.mockito.ide.refactoring.proposal.strategy.ConversionToRecordingStrategy;
import pl.greenpath.mockito.ide.refactoring.proposal.strategy.WhenThenReturnRecordingStrategy;
import pl.greenpath.mockito.ide.refactoring.proposal.strategy.WhenThenThrowRecordingStrategy;

public class ConvertToRecordingFix implements IQuickFix {

    @Override
    public boolean isApplicable(final IInvocationContext context, final IProblemLocation problemLocation) {
        final ExpressionStatement statement = new AstResolver().findParentOfType(context.getCoveringNode(),
                ExpressionStatement.class);

        return problemLocation.getProblemId() == IProblem.ParsingErrorInsertToComplete
                && statement != null && statement.getExpression() instanceof MethodInvocation;
    }

    @Override
    public List<IJavaCompletionProposal> getProposals(final IInvocationContext context, final IProblemLocation location) {
        return Arrays.asList(
                getConvertToRecording(context, location, new WhenThenReturnRecordingStrategy()),
                getConvertToRecording(context, location, new WhenThenThrowRecordingStrategy()));
    }

    private IJavaCompletionProposal getConvertToRecording(final IInvocationContext context,
            final IProblemLocation location, final ConversionToRecordingStrategy strategy) {
        final ASTNode selectedNode = new AstResolver().findParentOfType(context.getCoveringNode(),
                MethodInvocation.class);

        return new ConvertToMockRecordProposal(context.getCompilationUnit(), selectedNode, context.getASTRoot(),
                strategy);
    }

}
