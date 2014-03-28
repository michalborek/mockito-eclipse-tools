package pl.greenpath.mockito.ide.refactoring.quickassist.impl;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

import pl.greenpath.mockito.ide.refactoring.ast.AstResolver;
import pl.greenpath.mockito.ide.refactoring.proposal.ConvertToFieldMockProposal;

public class ConvertToFieldMockAssist implements IQuickFixAssist {

    @Override
    public boolean isApplicable(final IInvocationContext context) {
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
        for (final FieldDeclaration field : typeDeclaration.getFields()) {
            final VariableDeclarationFragment fragment = (VariableDeclarationFragment) field.fragments().get(0);
            if (fragment.getName().getIdentifier().equals(name.getIdentifier())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public IJavaCompletionProposal getProposal(final IInvocationContext context) {
        return new ConvertToFieldMockProposal(
                context.getCompilationUnit(),
                new AstResolver().findParentOfType(
                        context.getCoveringNode(), VariableDeclarationStatement.class), context.getASTRoot());
    }

}
