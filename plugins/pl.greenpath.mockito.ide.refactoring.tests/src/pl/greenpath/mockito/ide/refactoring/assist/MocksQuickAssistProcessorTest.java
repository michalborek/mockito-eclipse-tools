package pl.greenpath.mockito.ide.refactoring.assist;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import pl.greenpath.mockito.ide.refactoring.assist.impl.IQuickFixAssist;

@RunWith(MockitoJUnitRunner.class)
public class MocksQuickAssistProcessorTest extends MocksQuickAssistProcessor {

    @Mock
    private ICompilationUnit cuMock;

    @Mock
    private IInvocationContext contextMock;

    @Mock
    private IQuickFixAssist assistMock;
    
    @Mock
    private IQuickFixAssist anotherAssistMock;

    private MocksQuickAssistProcessor testedClass;

    private IProblemLocation[] locations;

    @Before
    public void before() {
        testedClass = new MocksQuickAssistProcessor(Arrays.asList(assistMock, anotherAssistMock));
        when(contextMock.getCompilationUnit()).thenReturn(cuMock);
        locations = new IProblemLocation[] { mock(IProblemLocation.class) };
    }

    @Test
    public void shouldHaveAssistsIfClassEndsWithTest() throws CoreException {
        final IType typeMock = mock(IType.class);
        when(typeMock.getElementName()).thenReturn("SomeTest");
        when(cuMock.findPrimaryType()).thenReturn(typeMock);

        assertThat(testedClass.hasAssists(contextMock)).isTrue();
    }

    @Test
    public void shouldNotHaveAssistIfClassNameNotEndsWithTest() throws CoreException {
        final IType typeMock = mock(IType.class);
        when(typeMock.getElementName()).thenReturn("SomeClass");
        when(cuMock.findPrimaryType()).thenReturn(typeMock);

        assertThat(testedClass.hasAssists(contextMock)).isFalse();
    }

    @Test
    public void shouldGetNoAssists_noApplicableConverter() throws CoreException {
        when(assistMock.isApplicable(contextMock)).thenReturn(Boolean.FALSE);

        assertThat(testedClass.getAssists(contextMock, locations)).isEmpty();
    }

    @Test
    public void shouldGetAssists_foundApplicableConverter() throws CoreException {
        when(assistMock.isApplicable(contextMock)).thenReturn(Boolean.TRUE);
        when(anotherAssistMock.isApplicable(contextMock)).thenReturn(Boolean.FALSE);
        final IJavaCompletionProposal proposalMock = mock(IJavaCompletionProposal.class);
        when(assistMock.getProposal(contextMock)).thenReturn(proposalMock);

        final IJavaCompletionProposal[] result = testedClass.getAssists(contextMock, locations);
        assertThat(result).hasSize(1).containsOnly(proposalMock);
    }

    @Test
    public void shouldGetAssists_foundMoreApplicableConverters() throws CoreException {
        final IJavaCompletionProposal proposalMock = mock(IJavaCompletionProposal.class);
        final IJavaCompletionProposal anotherProposalMock = mock(IJavaCompletionProposal.class);

        when(assistMock.isApplicable(contextMock)).thenReturn(Boolean.TRUE);
        when(anotherAssistMock.isApplicable(contextMock)).thenReturn(Boolean.TRUE);
        when(assistMock.getProposal(contextMock)).thenReturn(proposalMock);
        when(anotherAssistMock.getProposal(contextMock)).thenReturn(anotherProposalMock);

        final IJavaCompletionProposal[] result = testedClass.getAssists(contextMock, locations);
        assertThat(result).hasSize(2).containsOnly(proposalMock, anotherProposalMock);
    }
    
}
