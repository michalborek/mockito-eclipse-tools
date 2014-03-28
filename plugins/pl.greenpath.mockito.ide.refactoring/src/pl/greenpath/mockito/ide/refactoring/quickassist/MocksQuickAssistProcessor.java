package pl.greenpath.mockito.ide.refactoring.quickassist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;

import pl.greenpath.mockito.ide.refactoring.quickassist.impl.ConvertToFieldMockAssist;
import pl.greenpath.mockito.ide.refactoring.quickassist.impl.ConvertToSpyAssist;
import pl.greenpath.mockito.ide.refactoring.quickassist.impl.IQuickFixAssist;

public class MocksQuickAssistProcessor implements IQuickAssistProcessor {

    private final List<IQuickFixAssist> assists;

    public MocksQuickAssistProcessor() {
        this(Arrays.asList(new ConvertToFieldMockAssist(), new ConvertToSpyAssist()));
    }

    public MocksQuickAssistProcessor(final List<IQuickFixAssist> availableAssists) {
        assists = availableAssists;
    }

    @Override
    public boolean hasAssists(final IInvocationContext context) throws CoreException {
        return context.getCompilationUnit().findPrimaryType().getElementName().endsWith("Test");
    }

    @Override
    public IJavaCompletionProposal[] getAssists(final IInvocationContext context, final IProblemLocation[] locations)
            throws CoreException {
        final List<IJavaCompletionProposal> result = new ArrayList<>();

        for (final IQuickFixAssist assist : assists) {
            if (assist.isApplicable(context)) {
                result.add(assist.getProposal(context));
            }
        }
        return result.toArray(new IJavaCompletionProposal[result.size()]);
    }
}
