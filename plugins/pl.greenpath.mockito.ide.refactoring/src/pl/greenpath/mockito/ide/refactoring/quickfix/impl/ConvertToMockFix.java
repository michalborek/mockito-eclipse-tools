package pl.greenpath.mockito.ide.refactoring.quickfix.impl;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;

public abstract class ConvertToMockFix implements IQuickFix {

    @Override
    public abstract IJavaCompletionProposal getProposal(final IInvocationContext context,
            final IProblemLocation location);

    @Override
    public boolean isApplicable(final IInvocationContext context, final IProblemLocation problemLocation) {
        return problemLocation.getProblemId() == IProblem.UnresolvedVariable
                && problemLocation.getCoveredNode(context.getASTRoot()).getNodeType() == ASTNode.SIMPLE_NAME;
    }

    protected SimpleName getSelectedNode(final IInvocationContext context, final IProblemLocation location) {
        final ASTNode selectedNode = location.getCoveredNode(context.getASTRoot());
        return (SimpleName) selectedNode;
    }

}