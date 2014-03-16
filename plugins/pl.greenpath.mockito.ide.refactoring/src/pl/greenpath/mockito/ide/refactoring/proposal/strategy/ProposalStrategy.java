package pl.greenpath.mockito.ide.refactoring.proposal.strategy;

import org.eclipse.jdt.core.dom.Type;

public interface ProposalStrategy {

    Object getArgument(Type type);

    String getMockitoMethodName();

    String getVariableIdentifier();

}