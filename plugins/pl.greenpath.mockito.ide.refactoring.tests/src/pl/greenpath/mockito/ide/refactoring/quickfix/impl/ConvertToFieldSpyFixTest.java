package pl.greenpath.mockito.ide.refactoring.quickfix.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import pl.greenpath.mockito.ide.refactoring.proposal.AddFieldMockProposal;

@RunWith(MockitoJUnitRunner.class)
public class ConvertToFieldSpyFixTest {

    @Mock
    protected ICompilationUnit cuMock;

    @Mock
    protected IInvocationContext contextMock;

    @Mock
    protected CompilationUnit astRoot;

    protected ConvertToFieldSpyFix testedClass;

    @Before
    public void before() {
        when(contextMock.getCompilationUnit()).thenReturn(cuMock);
        when(contextMock.getASTRoot()).thenReturn(astRoot);
        testedClass = new ConvertToFieldSpyFix();
    }

    @Test
    public void shouldExtendConvertToMockFix() {
        assertThat(testedClass).isInstanceOf(ConvertToMockFix.class);
    }

    @Test
    public void shouldReturnAddLocalMockProposal() {
        final IProblemLocation location = mock(IProblemLocation.class);
        assertThat(testedClass.getProposal(contextMock, location)).isInstanceOf(AddFieldMockProposal.class);
    }

}
