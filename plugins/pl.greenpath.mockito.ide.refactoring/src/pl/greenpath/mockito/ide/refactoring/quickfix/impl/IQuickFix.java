package pl.greenpath.mockito.ide.refactoring.quickfix.impl;

import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;

public interface IQuickFix {

    boolean isApplicable(final IInvocationContext context, final IProblemLocation problemLocation);

    IJavaCompletionProposal getProposal(final IInvocationContext context, final IProblemLocation location);

}
