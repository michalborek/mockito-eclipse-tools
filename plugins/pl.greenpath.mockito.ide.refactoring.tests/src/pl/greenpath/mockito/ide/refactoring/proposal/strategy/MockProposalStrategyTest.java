package pl.greenpath.mockito.ide.refactoring.proposal.strategy;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.greenpath.mockito.ide.refactoring.TestUtils.AST_INSTANCE;

import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.junit.Test;

import pl.greenpath.mockito.ide.refactoring.TestUtils;

public class MockProposalStrategyTest {

    @SuppressWarnings("unchecked")
    @Test
    public void shouldReturnSimpleTypeWhenParametrizedTypeIsPassed() {
        final VariableDeclarationFragment variableDeclaration = TestUtils.createVariableDeclaration("foo");

        final VariableDeclarationStatement statement = AST_INSTANCE
                .newVariableDeclarationStatement(variableDeclaration);
        final ParameterizedType type = AST_INSTANCE.newParameterizedType(AST_INSTANCE.newSimpleType(AST_INSTANCE
                .newSimpleName("List")));
        type.typeArguments().add(AST_INSTANCE.newSimpleType(AST_INSTANCE.newSimpleName("Object")));
        statement.setType(type);

        final MockProposalStrategy testedClass = new MockProposalStrategy(variableDeclaration.getName());

        final TypeLiteral result = (TypeLiteral) testedClass.getArgument(type);
        assertThat(result.getType().isSimpleType()).isTrue();
        assertThat(((SimpleType)result.getType()).getName().getFullyQualifiedName()).isEqualTo("List");
    }
}
