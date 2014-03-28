package pl.greenpath.mockito.ide.refactoring.quickfix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;

import pl.greenpath.mockito.ide.refactoring.quickfix.impl.ConvertToMockFix;
import pl.greenpath.mockito.ide.refactoring.quickfix.impl.ConvertToRecordingFix;
import pl.greenpath.mockito.ide.refactoring.quickfix.impl.IQuickFix;

public class MocksQuickFixProcessor implements IQuickFixProcessor {

    private final List<IQuickFix> quickFixes;

    public MocksQuickFixProcessor() {
        this(Arrays.asList(new ConvertToMockFix(), new ConvertToRecordingFix()));
    }

    public MocksQuickFixProcessor(final List<IQuickFix> supportedFixes) {
        quickFixes = supportedFixes;
    }

    @Override
    public IJavaCompletionProposal[] getCorrections(final IInvocationContext context, final IProblemLocation[] locations)
            throws CoreException {
        final List<IJavaCompletionProposal> corrections = new ArrayList<>();

        for (final IProblemLocation location : locations) {
            for (final IQuickFix fix : quickFixes) {
                if (fix.isApplicable(context, location)) {
                    corrections.addAll(fix.getProposals(context, location));
                }
            }
        }
        return corrections.toArray(new IJavaCompletionProposal[corrections.size()]);
    }

    @Override
    public boolean hasCorrections(final ICompilationUnit unit, final int problemId) {
        return unit.findPrimaryType().getElementName().endsWith("Test");
    }

}
