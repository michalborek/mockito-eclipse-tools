package pl.greenpath.mockito.ide.refactoring.assisst;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;

import pl.greenpath.mockito.ide.refactoring.ast.AstResolver;
import pl.greenpath.mockito.ide.refactoring.proposal.AddLocalMockitoProposal;
import pl.greenpath.mockito.ide.refactoring.proposal.ConvertToFieldMockProposal;
import pl.greenpath.mockito.ide.refactoring.proposal.strategy.SpyProposalStrategy;

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
        else if(isConvertToSpyPossile(context.getCoveringNode())){
        	return new IJavaCompletionProposal[] { getConvertToSpyAssist(context) };
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
        return new ConvertToFieldMockProposal(
                context.getCompilationUnit(), 
                new AstResolver().findParentOfType(
                context.getCoveringNode(), VariableDeclarationStatement.class), context.getASTRoot());
    }

    public IJavaCompletionProposal getConvertToSpyAssist(final IInvocationContext context) {
    	final SimpleName coveredNode = (SimpleName) context.getCoveringNode();
		return new AddLocalMockitoProposal(context.getCompilationUnit(), coveredNode, context.getASTRoot(), 
				new SpyProposalStrategy(coveredNode));
    	
    }
    
    private boolean isConvertToSpyPossile(final ASTNode coveringNode) {
        
    	if(coveringNode.getNodeType() == ASTNode.SIMPLE_NAME){
    		final SimpleName simpleName = (SimpleName) coveringNode;
    		final IBinding resolveBinding = simpleName.resolveBinding();
    		
    		if(resolveBinding == null || resolveBinding.getKind() != IBinding.VARIABLE){
    			return false;
    		}
    		if (!isInvokedInsideMethod(simpleName)) {
    		    return false;
    		}
			if (hasLocalFieldWithName(simpleName, simpleName.getIdentifier() + "Spy")) {
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
    private boolean hasLocalFieldWithName(final SimpleName selection, final String newName) {
    	final MethodDeclaration methodDeclaration = new AstResolver().findParentOfType(selection, MethodDeclaration.class);
    	if(methodDeclaration == null) {
    	    return false;
    	}
    	for (final Statement statement : (List<Statement>) methodDeclaration.getBody().statements()) {
    		if( Statement.VARIABLE_DECLARATION_STATEMENT == statement.getNodeType()){
    			final VariableDeclarationFragment fragment = (VariableDeclarationFragment) ((VariableDeclarationStatement)statement).fragments().get(0);
    			if(fragment.getName().getIdentifier().equals(newName)){
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
}
