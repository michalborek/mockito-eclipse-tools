package pl.greenpath.mockito.ide.refactoring.proposal;

import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;

public interface ProposalStrategy {

	Object getArgument(Type type);

	String getMockitoMethodName();

	String getVariableIdentifier();

}