package pl.greenpath.mockito.ide.refactoring.quickfix;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;

import pl.greenpath.mockito.ide.refactoring.ast.ContextBaseTypeFinder;
import pl.greenpath.mockito.ide.refactoring.proposal.AddLocalMockProposal;
import pl.greenpath.mockito.ide.refactoring.proposal.AddMockFieldProposal;
import pl.greenpath.mockito.ide.refactoring.quickfix.exception.NotSupportedRefactoring;

public class MocksQuickFixProcessor implements IQuickFixProcessor {

    @Override
    public IJavaCompletionProposal[] getCorrections(final IInvocationContext context, final IProblemLocation[] locations)
            throws CoreException {
        final List<IJavaCompletionProposal> corrections = new ArrayList<IJavaCompletionProposal>();

        for (final IProblemLocation location : locations) {
            if (hasCorrections(context.getCompilationUnit(), location.getProblemId())) {
                addProposals(context, corrections, location);
            }
        }
        return corrections.toArray(new IJavaCompletionProposal[0]);
    }

    private void addProposals(final IInvocationContext context, final List<IJavaCompletionProposal> corrections,
            final IProblemLocation location) {
        try {
            corrections.add(getAddFieldMockProposal(context, location));
            corrections.add(getAddLocalMockProposal(context, location));
        } catch (final NotSupportedRefactoring e) {
            // TODO logging
            e.printStackTrace();
        }
    }

    private IJavaCompletionProposal getAddLocalMockProposal(final IInvocationContext context,
            final IProblemLocation location) throws NotSupportedRefactoring {
        final ASTNode selectedNode = location.getCoveredNode(context.getASTRoot());
        if (selectedNode.getNodeType() != ASTNode.SIMPLE_NAME) {
            throw new NotSupportedRefactoring("This selection is not supported by this refactoring");
        }
        return new AddLocalMockProposal(context.getCompilationUnit(), (SimpleName) selectedNode,
                new ContextBaseTypeFinder((SimpleName) selectedNode).find(), context.getASTRoot());
    }

    private AddMockFieldProposal getAddFieldMockProposal(final IInvocationContext context,
            final IProblemLocation location)
            throws NotSupportedRefactoring {
        final ASTNode selectedNode = location.getCoveredNode(context.getASTRoot());
        if (selectedNode.getNodeType() != ASTNode.SIMPLE_NAME) {
            throw new NotSupportedRefactoring("This selection is not supported by this refactoring");
        }
        return new AddMockFieldProposal(context.getCompilationUnit(), (SimpleName) selectedNode,
                new ContextBaseTypeFinder((SimpleName) selectedNode).find(), context.getASTRoot());
    }

    @Override
    public boolean hasCorrections(final ICompilationUnit unit, final int problemId) {
        return problemId == IProblem.UnresolvedVariable
                && unit.findPrimaryType().getElementName().endsWith("Test");
    }

}
