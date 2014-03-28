package pl.greenpath.mockito.ide.refactoring.quickfix.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;

import pl.greenpath.mockito.ide.refactoring.proposal.AddLocalMockitoProposal;
import pl.greenpath.mockito.ide.refactoring.proposal.AddMockitoFieldProposal;
import pl.greenpath.mockito.ide.refactoring.proposal.strategy.MockProposalStrategy;
import pl.greenpath.mockito.ide.refactoring.proposal.strategy.ProposalStrategy;
import pl.greenpath.mockito.ide.refactoring.quickfix.exception.NotSupportedRefactoringException;

public class ConvertToMockFix implements IQuickFix {

    @Override
    public boolean isApplicable(final IInvocationContext context, final IProblemLocation problemLocation) {
        return problemLocation.getProblemId() == IProblem.UnresolvedVariable;
    }

    
    @Override
    public List<IJavaCompletionProposal> getProposals(final IInvocationContext context,
            final IProblemLocation location) {
        try {
            return Arrays.asList(
                    getAddFieldMockitoProposal(context, location, "Mock"),
                    getAddFieldMockitoProposal(context, location, "Spy"),
                    getAddLocalMockitoProposal(context, location,
                            new MockProposalStrategy(getSelectedNode(context, location))));
        } catch (final NotSupportedRefactoringException e) {
            // TODO logging
            e.printStackTrace();
            return Collections.emptyList();
        }
    }


    private IJavaCompletionProposal getAddLocalMockitoProposal(final IInvocationContext context,
            final IProblemLocation location, final ProposalStrategy proposalStrategy) throws NotSupportedRefactoringException {
        final SimpleName selectedNode = getSelectedNode(context, location);
        return new AddLocalMockitoProposal(context.getCompilationUnit(), selectedNode, context.getASTRoot(),
                proposalStrategy);
    }


    private AddMockitoFieldProposal getAddFieldMockitoProposal(final IInvocationContext context,
            final IProblemLocation location, final String mockitoAnnotation)
            throws NotSupportedRefactoringException {
        final ASTNode selectedNode = getSelectedNode(context, location);
        return new AddMockitoFieldProposal(context.getCompilationUnit(), (SimpleName) selectedNode,
                context.getASTRoot(), mockitoAnnotation);
    }


    private SimpleName getSelectedNode(final IInvocationContext context,
            final IProblemLocation location) throws NotSupportedRefactoringException {
        final ASTNode selectedNode = location.getCoveredNode(context.getASTRoot());
        if (selectedNode.getNodeType() != ASTNode.SIMPLE_NAME) {
            throw new NotSupportedRefactoringException("This selection is not supported by this refactoring");
        }
        return (SimpleName) selectedNode;
    }
}
