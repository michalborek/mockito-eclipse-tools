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

import pl.greenpath.mockito.ide.refactoring.proposal.AddLocalMockProposal;

@RunWith(MockitoJUnitRunner.class)
public class ConvertToLocalMockFixTest {

    @Mock
    private ICompilationUnit cuMock;

    @Mock
    private IInvocationContext contextMock;

    @Mock
    private CompilationUnit astRoot;

    private ConvertToLocalMockFix testedClass;

    @Before
    public void before() {
        when(contextMock.getCompilationUnit()).thenReturn(cuMock);
        when(contextMock.getASTRoot()).thenReturn(astRoot);
        testedClass = new ConvertToLocalMockFix();
    }

    @Test
    public void shouldExtendConvertToMockFix() {
        assertThat(testedClass).isInstanceOf(ConvertToMockFix.class);
    }

    @Test
    public void shouldReturnAddLocalMockProposal() {
        final IProblemLocation location = mock(IProblemLocation.class);
        assertThat(testedClass.getProposal(contextMock, location)).isInstanceOf(AddLocalMockProposal.class);
    }

}
