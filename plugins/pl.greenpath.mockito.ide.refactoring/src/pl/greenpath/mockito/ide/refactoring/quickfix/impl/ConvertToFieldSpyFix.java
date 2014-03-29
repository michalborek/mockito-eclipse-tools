package pl.greenpath.mockito.ide.refactoring.quickfix.impl;

import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;

import pl.greenpath.mockito.ide.refactoring.proposal.AddFieldMockProposal;

public class ConvertToFieldSpyFix extends ConvertToMockFix {

    @Override
    public IJavaCompletionProposal getProposal(final IInvocationContext context, final IProblemLocation location) {
        return new AddFieldMockProposal(context.getCompilationUnit(), getSelectedNode(context, location),
                context.getASTRoot(), "Spy");
    }

}
