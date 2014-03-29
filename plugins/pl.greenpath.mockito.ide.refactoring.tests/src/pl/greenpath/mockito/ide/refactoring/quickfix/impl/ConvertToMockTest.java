package pl.greenpath.mockito.ide.refactoring.quickfix.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import pl.greenpath.mockito.ide.refactoring.TestUtils;

@RunWith(MockitoJUnitRunner.class)
public class ConvertToMockTest {

    @Mock
    protected ICompilationUnit cuMock;

    @Mock
    protected IInvocationContext contextMock;

    @Mock
    protected CompilationUnit astRoot;

    private ConvertToMockFix testedClass;

    @Before
    public void before() {
        when(contextMock.getCompilationUnit()).thenReturn(cuMock);
        when(contextMock.getASTRoot()).thenReturn(astRoot);
        testedClass = new ConvertToLocalMockFix();
    }

    @Test
    public void shouldBeApplicable_unresolvedVariable() {
        final IProblemLocation problemLocation = mock(IProblemLocation.class);
        when(problemLocation.getProblemId()).thenReturn(IProblem.UnresolvedVariable);
        when(problemLocation.getCoveredNode(astRoot)).thenReturn(TestUtils.AST_INSTANCE.newSimpleName("testMock"));
        assertThat(testedClass.isApplicable(contextMock, problemLocation)).isTrue();
    }

    @Test
    public void shouldNotBeApplicable_notUnresolvedVariable() {
        final IProblemLocation problemLocation = mock(IProblemLocation.class);
        when(problemLocation.getProblemId()).thenReturn(IProblem.AmbiguousConstructor);
        when(problemLocation.getCoveredNode(astRoot)).thenReturn(TestUtils.AST_INSTANCE.newSimpleName("testMock"));
        assertThat(testedClass.isApplicable(contextMock, problemLocation)).isFalse();
    }

    @Test
    public void shouldNotBeApplicable_selectionIsNotASimpleVariableName() {
        final IProblemLocation problemLocation = mock(IProblemLocation.class);
        when(problemLocation.getProblemId()).thenReturn(IProblem.UnresolvedVariable);
        when(problemLocation.getCoveredNode(astRoot)).thenReturn(TestUtils.AST_INSTANCE.newStringLiteral());
        assertThat(testedClass.isApplicable(contextMock, problemLocation)).isFalse();
    }
}
