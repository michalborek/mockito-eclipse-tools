package pl.greenpath.mockito.ide.refactoring.proposal.strategy;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

public interface ProposalStrategy {

    ASTNode getArgument(Type type);

    String getMockitoMethodName();

    String getVariableIdentifier();

}