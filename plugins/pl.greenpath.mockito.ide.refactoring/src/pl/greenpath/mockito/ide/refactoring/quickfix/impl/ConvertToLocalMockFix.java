package pl.greenpath.mockito.ide.refactoring.quickfix.impl;

import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;

import pl.greenpath.mockito.ide.refactoring.proposal.AddLocalMockProposal;
import pl.greenpath.mockito.ide.refactoring.proposal.strategy.MockProposalStrategy;

public class ConvertToLocalMockFix extends ConvertToMockFix {

    @Override
    public IJavaCompletionProposal getProposal(final IInvocationContext context,
            final IProblemLocation location) {
        final MockProposalStrategy proposalStrategy = new MockProposalStrategy(getSelectedNode(context, location));
        return new AddLocalMockProposal(context.getCompilationUnit(), getSelectedNode(context, location),
                context.getASTRoot(), proposalStrategy);

    }
}
