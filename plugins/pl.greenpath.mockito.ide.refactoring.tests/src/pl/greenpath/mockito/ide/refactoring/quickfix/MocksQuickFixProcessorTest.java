package pl.greenpath.mockito.ide.refactoring.quickfix;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import pl.greenpath.mockito.ide.refactoring.TestUtils;
import pl.greenpath.mockito.ide.refactoring.quickfix.impl.IQuickFix;

@RunWith(MockitoJUnitRunner.class)
public class MocksQuickFixProcessorTest {

    @Mock
    private ICompilationUnit cuMock;

    @Mock
    private IInvocationContext contextMock;

    @Mock
    private IQuickFix fixMock1;

    @Mock
    private IQuickFix fixMock2;

    @Mock
    private CompilationUnit astRoot;

    private MocksQuickFixProcessor testedClass;

    @Before
    public void before() {
        when(contextMock.getASTRoot()).thenReturn(astRoot);
        when(contextMock.getCompilationUnit()).thenReturn(cuMock);
        testedClass = new MocksQuickFixProcessor(Arrays.asList(fixMock1, fixMock2));
    }

    @Test
    public void shouldCollectFixesIfIsApplicableReturnsTrue() throws CoreException {
        final IProblemLocation location = mock(IProblemLocation.class);
        final IProblemLocation[] locations = new IProblemLocation[] { location };
        when(fixMock1.isApplicable(contextMock, location)).thenReturn(Boolean.TRUE);
        when(fixMock2.isApplicable(contextMock, location)).thenReturn(Boolean.FALSE);
        final IJavaCompletionProposal proposal1 = mock(IJavaCompletionProposal.class);
        when(fixMock1.getProposal(contextMock, location)).thenReturn(proposal1);

        final IJavaCompletionProposal[] corrections = testedClass.getCorrections(contextMock, locations);

        assertThat(corrections).containsOnly(proposal1);
    }

    @Test
    public void shouldReturnEmptyArrayWhenNoFixesAreApplicable() throws CoreException {
        final IProblemLocation location = mock(IProblemLocation.class);
        final IProblemLocation[] locations = new IProblemLocation[] { location };
        when(fixMock1.isApplicable(contextMock, location)).thenReturn(Boolean.FALSE);
        when(fixMock2.isApplicable(contextMock, location)).thenReturn(Boolean.FALSE);

        final IJavaCompletionProposal[] corrections = testedClass.getCorrections(contextMock, locations);

        assertThat(corrections).isEmpty();
    }

    @Test
    public void shouldCollectResultsWhenMoreThanOneIsApplicable() throws CoreException {
        final IProblemLocation location = mock(IProblemLocation.class);
        final IProblemLocation[] locations = new IProblemLocation[] { location };
        when(fixMock1.isApplicable(contextMock, location)).thenReturn(Boolean.TRUE);
        when(fixMock2.isApplicable(contextMock, location)).thenReturn(Boolean.TRUE);

        final IJavaCompletionProposal proposal1 = mock(IJavaCompletionProposal.class);
        final IJavaCompletionProposal proposal2 = mock(IJavaCompletionProposal.class);
        when(fixMock1.getProposal(contextMock, location)).thenReturn(proposal1);
        when(fixMock2.getProposal(contextMock, location)).thenReturn(proposal2);

        final IJavaCompletionProposal[] corrections = testedClass.getCorrections(contextMock, locations);

        assertThat(corrections).containsOnly(proposal1, proposal2);
    }

    @Test
    public void shouldHaveAssistsIfClassEndsWithTest() throws CoreException {
        final IType typeMock = mock(IType.class);
        when(typeMock.getElementName()).thenReturn("SomeTest");
        when(cuMock.findPrimaryType()).thenReturn(typeMock);

        assertThat(testedClass.hasCorrections(cuMock, 0)).isTrue();
    }

    @Test
    public void shouldNotHaveAssistIfClassNameNotEndsWithTest() throws CoreException {
        final IType typeMock = mock(IType.class);
        when(typeMock.getElementName()).thenReturn("SomeClass");
        when(cuMock.findPrimaryType()).thenReturn(typeMock);

        assertThat(testedClass.hasCorrections(cuMock, 0)).isFalse();
    }

    @Test
    public void shouldReturnConvertToMockIfApplicable() throws CoreException {
        final MocksQuickFixProcessor processor = new MocksQuickFixProcessor();
        final VariableDeclarationFragment fragment = TestUtils.createVariableDeclaration("Object", "someObject");
        TestUtils.createVariableDeclarationStatement(fragment);
        final IProblemLocation location = mock(IProblemLocation.class);
        when(location.getProblemId()).thenReturn(IProblem.UnresolvedVariable);
        when(location.getCoveredNode(astRoot)).thenReturn(fragment.getName());
        final IJavaCompletionProposal[] corrections = processor.getCorrections(contextMock,
                new IProblemLocation[] { location });
        assertThat(corrections).hasSize(3);
    }

}
