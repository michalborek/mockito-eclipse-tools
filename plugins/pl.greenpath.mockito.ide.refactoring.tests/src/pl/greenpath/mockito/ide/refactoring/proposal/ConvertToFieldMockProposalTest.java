package pl.greenpath.mockito.ide.refactoring.proposal;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import pl.greenpath.mockito.ide.refactoring.TestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ConvertToFieldMockProposalTest {

    @Mock
    private ICompilationUnit cu;

    @Mock
    private CompilationUnit astRoot;

    @Test
    public void shouldOverrideToString() {
        final VariableDeclarationStatement selectedNode = 
                TestUtils.createVariableDeclarationStatement(TestUtils.createVariableDeclaration("Object", "test"));
        final ConvertToFieldMockProposal testedClass = new ConvertToFieldMockProposal(cu, selectedNode , astRoot);
        assertThat(testedClass.toString()).isEqualTo("ConvertToFieldMockProposal [selectedStatement=\"Object test;\n\"]");
    }
}
