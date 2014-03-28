package pl.greenpath.mockito.ide.refactoring.quickfix.exception;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import pl.greenpath.mockito.ide.refactoring.Activator;

public class NotSupportedRefactoringException extends CoreException {

    public NotSupportedRefactoringException(final String message) {
        super(new Status(IStatus.ERROR, Activator.PLUGIN_ID, message));
    }

}
