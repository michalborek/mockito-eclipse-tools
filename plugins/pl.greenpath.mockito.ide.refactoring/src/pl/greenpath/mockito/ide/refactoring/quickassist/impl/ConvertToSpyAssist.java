package pl.greenpath.mockito.ide.refactoring.quickassist.impl;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;

import pl.greenpath.mockito.ide.refactoring.ast.AstResolver;
import pl.greenpath.mockito.ide.refactoring.proposal.AddLocalMockProposal;
import pl.greenpath.mockito.ide.refactoring.proposal.strategy.SpyProposalStrategy;

public class ConvertToSpyAssist implements IQuickFixAssist {

    @Override
    public boolean isApplicable(final IInvocationContext context) {
        if (context.getCoveringNode().getNodeType() == ASTNode.SIMPLE_NAME) {
            final SimpleName simpleName = (SimpleName) context.getCoveringNode();
            final IBinding resolveBinding = simpleName.resolveBinding();

            if (resolveBinding == null || resolveBinding.getKind() != IBinding.VARIABLE) {
                return false;
            }
            if (!isInvokedInsideMethod(simpleName)) {
                return false;
            }
            if (hasConflictingFieldWithName(simpleName, simpleName.getIdentifier() + "Spy")) {
                return false;
            }
            if (!hasAssigment(simpleName)) {
                return false;
            }
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private boolean hasConflictingFieldWithName(final SimpleName selection, final String newName) {
        final MethodDeclaration methodDeclaration = new AstResolver().findParentOfType(selection,
                MethodDeclaration.class);
        if (methodDeclaration == null) {
            return false;
        }
        for (final Statement statement : (List<Statement>) methodDeclaration.getBody().statements()) {
            if (Statement.VARIABLE_DECLARATION_STATEMENT == statement.getNodeType()) {
                final VariableDeclarationFragment fragment = (VariableDeclarationFragment) ((VariableDeclarationStatement) statement)
                        .fragments().get(0);
                if (fragment.getName().getIdentifier().equals(newName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isInvokedInsideMethod(final SimpleName simpleName) {
        return new AstResolver().findParentOfType(simpleName, MethodDeclaration.class) != null;
    }

    private boolean hasAssigment(final SimpleName simpleName) {
        return new AstResolver().findParentOfType(simpleName, Assignment.class) != null;
    }

    @Override
    public IJavaCompletionProposal getProposal(final IInvocationContext context) {
        final SimpleName coveredNode = (SimpleName) context.getCoveringNode();
        return new AddLocalMockProposal(context.getCompilationUnit(), coveredNode, context.getASTRoot(),
                new SpyProposalStrategy(coveredNode));

    }

}
