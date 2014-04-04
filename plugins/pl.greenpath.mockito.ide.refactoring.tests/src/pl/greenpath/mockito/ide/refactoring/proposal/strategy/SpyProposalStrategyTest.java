package pl.greenpath.mockito.ide.refactoring.proposal.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.junit.Before;
import org.junit.Test;

import pl.greenpath.mockito.ide.refactoring.TestUtils;

public class SpyProposalStrategyTest {

    private SpyProposalStrategy testedClass;
    private SimpleName variable;

    @Before
    public void before() {
        variable = TestUtils.createVariableDeclaration("foo").getName();
        testedClass = new SpyProposalStrategy(variable);
    }

    @Test
    public void shouldReturnSpyMethodName() {
        assertThat(testedClass.getMockitoMethodName()).isEqualTo("spy");
    }

    @Test
    public void shouldReturnCloneOfVariableAsArgument() {
        final Type type = mock(Type.class);
        final ASTNode argument = testedClass.getArgument(type);
        assertThat(argument).isNotSameAs(variable);
        assertThat(argument.getNodeType()).isEqualTo(variable.getNodeType());
        assertThat(argument).isInstanceOfAny(SimpleName.class);
        assertThat(((SimpleName)argument).getIdentifier()).isEqualTo(variable.getIdentifier());
    }
    
    @Test
    public void variableIdentifierShouldEndWithSpy() {
        assertThat(testedClass.getVariableIdentifier()).isEqualTo("fooSpy");
    }
    
    @Test
    public void shouldOverrideToString() {
        assertThat(testedClass.toString()).isEqualTo("SpyProposalStrategy [_selectedNode=foo]");
    }

}
