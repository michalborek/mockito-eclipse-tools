package pl.greenpath.mockito.ide.refactoring.quickassist.impl;

import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

public interface IQuickFixAssist {

    public abstract boolean isApplicable(IInvocationContext context);

    public abstract IJavaCompletionProposal getProposal(IInvocationContext context);

}