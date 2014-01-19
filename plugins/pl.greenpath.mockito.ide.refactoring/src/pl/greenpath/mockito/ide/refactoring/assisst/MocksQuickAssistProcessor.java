package pl.greenpath.mockito.ide.refactoring.assisst;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;

import pl.greenpath.mockito.ide.refactoring.ast.AstResolver;
import pl.greenpath.mockito.ide.refactoring.proposal.ConvertToFieldMockProposal;

public class MocksQuickAssistProcessor implements IQuickAssistProcessor {

    @Override
    public boolean hasAssists(final IInvocationContext context) throws CoreException {
        return context.getCompilationUnit().findPrimaryType().getElementName().endsWith("Test");
    }

    @Override
    public IJavaCompletionProposal[] getAssists(final IInvocationContext context, final IProblemLocation[] locations)
            throws CoreException {
        if (isConvertToFieldPossile(context)) {
            return new IJavaCompletionProposal[] { getConvertToMockAssist(context) };
        }
        return new IJavaCompletionProposal[0];
    }

    private boolean isConvertToFieldPossile(final IInvocationContext context) {
        final VariableDeclarationStatement statement = new AstResolver().findParentOfType(context.getCoveringNode(),
                VariableDeclarationStatement.class);
        if (statement == null) {
            return false;
        }

        final VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) statement
                .fragments().get(0);
        final Expression initializer = variableDeclarationFragment.getInitializer();
        if (!(initializer instanceof MethodInvocation)) {
            return false;
        }
        if (hasFieldWithName(variableDeclarationFragment.getName())) {
            return false;
        }
        return ((MethodInvocation) initializer).getName().getIdentifier().equals("mock");
    }

    private boolean hasFieldWithName(final SimpleName name) {
        final TypeDeclaration typeDeclaration = new AstResolver().findParentOfType(name, TypeDeclaration.class);
        for(final FieldDeclaration field: typeDeclaration.getFields()) {
            final VariableDeclarationFragment fragment = (VariableDeclarationFragment)field.fragments().get(0);
            if(fragment.getName().getIdentifier().equals(name.getIdentifier())) {
                return true;
            }
        }

        return false;
    }

    public IJavaCompletionProposal getConvertToMockAssist(final IInvocationContext context) {
        return new ConvertToFieldMockProposal(context.getCompilationUnit(), new AstResolver().findParentOfType(
                context.getCoveringNode(), VariableDeclarationStatement.class), context.getASTRoot());
    }

}
