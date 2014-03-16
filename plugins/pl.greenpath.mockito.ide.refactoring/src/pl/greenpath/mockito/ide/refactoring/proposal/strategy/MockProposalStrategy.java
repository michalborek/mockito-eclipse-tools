package pl.greenpath.mockito.ide.refactoring.proposal.strategy;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;

public class MockProposalStrategy implements ProposalStrategy {

	private final  SimpleName selectedNode;
	private final AST ast;
	private final static String MOCKITO_METHOD_NAME = "mock";
	
	public MockProposalStrategy(final SimpleName selectedNode) {
		this.selectedNode = selectedNode;
		ast = selectedNode.getAST();
	}
	

	@Override
	public Object getArgument(final Type type) {
        final TypeLiteral result = ast.newTypeLiteral();
        result.setType((Type) ASTNode.copySubtree(ast, getTypeForTypeLiteral(type)));
        return result;
    }
	
	private Type getTypeForTypeLiteral(final Type type) {
        if (type.isParameterizedType()) {
            return ((ParameterizedType) type).getType();
        } else {
            return type;
        }
    }
	
	@Override
	public String getMockitoMethodName() {
		return MOCKITO_METHOD_NAME;
	}


	@Override
	public String getVariableIdentifier() {
		return this.selectedNode.getIdentifier();
	}
}
