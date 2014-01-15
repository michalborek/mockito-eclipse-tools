package pl.greenpath.mockito.ide.refactoring.proposal;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;

public class SpyProposalStrategy implements ProposalStrategy {

	private  SimpleName _selectedNode;
	private final AST _ast;
	private final static String SPY_NAME = "Spy";
	
	public SpyProposalStrategy(final SimpleName selectedNode) {
		_selectedNode = selectedNode;
		_ast = selectedNode.getAST();
	}
	
	@Override
	public Object getArgument(final Type type) {
		return ASTNode.copySubtree(_ast, _selectedNode);
		
    }
	
	@Override
	public String getVariableIdentifier(){
		return _selectedNode.getIdentifier() + SPY_NAME;
	}
	@Override
	public String getMockitoMethodName() {
		return SPY_NAME.toLowerCase();
	}
}
