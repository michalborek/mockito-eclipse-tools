package pl.greenpath.mockito.ide.refactoring.quickfix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

import pl.greenpath.mockito.ide.refactoring.proposal.AddLocalMockProposal;
import pl.greenpath.mockito.ide.refactoring.proposal.AddMockFieldProposal;
import pl.greenpath.mockito.ide.refactoring.quickfix.exception.NotSupportedRefactoring;

public class MocksQuickFixProcessor implements IQuickFixProcessor {

    @Override
    public IJavaCompletionProposal[] getCorrections(final IInvocationContext context, final IProblemLocation[] locations)
            throws CoreException {
        final List<IJavaCompletionProposal> corrections = new ArrayList<IJavaCompletionProposal>();

        for (final IProblemLocation location : locations) {
            if (isUnresolvedVariable(location.getProblemId())) {
                corrections.addAll(getMockCreationProposals(context, location));
            } else if (isLocalMockDefinition(context)) {
                corrections.add(getMockConversionProposal(context, location));
            }
        }
        return corrections.toArray(new IJavaCompletionProposal[0]);
    }

    private IJavaCompletionProposal getMockConversionProposal(final IInvocationContext context,
            final IProblemLocation location) {
        return null; // TODO
    }

    private boolean isLocalMockDefinition(final IInvocationContext context) {
        // TODO Auto-generated method stub
        return false;
    }

    private boolean isUnresolvedVariable(final int problemId) {
        return problemId == IProblem.UnresolvedVariable;
    }

    private List<IJavaCompletionProposal> getMockCreationProposals(final IInvocationContext context,
            final IProblemLocation location) {
        try {
            return Arrays.asList(
                    getAddFieldMockProposal(context, location),
                    getAddLocalMockProposal(context, location));
        } catch (final NotSupportedRefactoring e) {
            // TODO logging
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private IJavaCompletionProposal getAddLocalMockProposal(final IInvocationContext context,
            final IProblemLocation location) throws NotSupportedRefactoring {
        final ASTNode selectedNode = location.getCoveredNode(context.getASTRoot());
        if (selectedNode.getNodeType() != ASTNode.SIMPLE_NAME) {
            throw new NotSupportedRefactoring("This selection is not supported by this refactoring");
        }
        return new AddLocalMockProposal(context.getCompilationUnit(), (SimpleName) selectedNode, context.getASTRoot());
    }

    private AddMockFieldProposal getAddFieldMockProposal(final IInvocationContext context,
            final IProblemLocation location)
            throws NotSupportedRefactoring {
        final ASTNode selectedNode = location.getCoveredNode(context.getASTRoot());
        if (selectedNode.getNodeType() != ASTNode.SIMPLE_NAME) {
            throw new NotSupportedRefactoring("This selection is not supported by this refactoring");
        }
        return new AddMockFieldProposal(context.getCompilationUnit(), (SimpleName) selectedNode, context.getASTRoot());
    }

    @Override
    public boolean hasCorrections(final ICompilationUnit unit, final int problemId) {
        return unit.findPrimaryType().getElementName().endsWith("Test");
    }

}
