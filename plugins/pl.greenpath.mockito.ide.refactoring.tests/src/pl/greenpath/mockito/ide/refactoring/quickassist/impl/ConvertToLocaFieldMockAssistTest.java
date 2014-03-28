package pl.greenpath.mockito.ide.refactoring.quickassist.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import pl.greenpath.mockito.ide.refactoring.proposal.ConvertToFieldMockProposal;
import pl.greenpath.mockito.ide.refactoring.quickassist.impl.ConvertToFieldMockAssist;

@RunWith(MockitoJUnitRunner.class)
public class ConvertToLocaFieldMockAssistTest {

    @Mock
    private CompilationUnit astRootMock;

    @Mock
    private ICompilationUnit cuMock;

    @Mock
    private IInvocationContext contextMock;

    private ConvertToFieldMockAssist testedClass;

    private final AST ast = TestUtils.AST_INSTANCE;

    @Before
    public void before() {
        when(contextMock.getCompilationUnit()).thenReturn(cuMock);
        when(contextMock.getASTRoot()).thenReturn(astRootMock);
        testedClass = new ConvertToFieldMockAssist();
    }

    @Test
    public void shouldBeApplicable_forLocalMockDeclaration() throws CoreException {
        final VariableDeclarationFragment variableDeclaration = TestUtils.createVariableDeclaration("Object", "type");
        variableDeclaration.setInitializer(TestUtils.getMethodInvocation("mock", "Object"));

        when(contextMock.getCoveringNode()).thenReturn(TestUtils.getVariableDeclarationStatement(variableDeclaration));

        assertThat(testedClass.isApplicable(contextMock)).isTrue();
    }

    @Test
    public void shouldNotBeApplicable_fieldNameConflictsWithExistingOne() throws CoreException {
        final VariableDeclarationFragment variableDeclaration = TestUtils.createVariableDeclaration("Object",
                "conflicting");
        variableDeclaration.setInitializer(TestUtils.getMethodInvocation("mock", "Object"));

        when(contextMock.getCoveringNode()).thenReturn(TestUtils.getVariableDeclarationStatement(variableDeclaration));

        assertThat(testedClass.isApplicable(contextMock)).isFalse();
    }

    @Test
    public void shouldNotBeApplicable_convertToFieldProposalIsNotLocalMock() throws CoreException {
        final VariableDeclarationFragment variableDeclaration = TestUtils.createVariableDeclaration("Object", "type");
        variableDeclaration.setInitializer(TestUtils.getMethodInvocation("notAMock", "Object"));

        when(contextMock.getCoveringNode()).thenReturn(TestUtils.getVariableDeclarationStatement(variableDeclaration));

        assertThat(testedClass.isApplicable(contextMock)).isFalse();
    }

    @Test
    public void shouldNotBeApplicable_convertToFieldProposalIfNotMethodInvocationPresent() throws CoreException {
        final VariableDeclarationFragment variableDeclaration = TestUtils.createVariableDeclaration("Object", "type");
        final ClassInstanceCreation newClassInstanceCreation = ast.newClassInstanceCreation();
        newClassInstanceCreation.setType(ast.newSimpleType(ast.newSimpleName("Object")));
        variableDeclaration.setInitializer(newClassInstanceCreation);

        when(contextMock.getCoveringNode()).thenReturn(TestUtils.getVariableDeclarationStatement(variableDeclaration));

        assertThat(testedClass.isApplicable(contextMock)).isFalse();
    }

    @Test
    public void shouldReturnProposal() {
        assertThat(testedClass.getProposal(contextMock)).isInstanceOf(ConvertToFieldMockProposal.class);
    }

}
