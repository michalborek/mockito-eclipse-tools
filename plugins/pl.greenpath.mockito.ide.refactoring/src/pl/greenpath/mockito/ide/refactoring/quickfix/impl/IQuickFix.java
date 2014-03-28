package pl.greenpath.mockito.ide.refactoring.quickfix.impl;

import java.util.List;

import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;

public interface IQuickFix {

    boolean isApplicable(final IInvocationContext context, final IProblemLocation problemLocation);

    List<IJavaCompletionProposal> getProposals(final IInvocationContext context, final IProblemLocation location);

}
