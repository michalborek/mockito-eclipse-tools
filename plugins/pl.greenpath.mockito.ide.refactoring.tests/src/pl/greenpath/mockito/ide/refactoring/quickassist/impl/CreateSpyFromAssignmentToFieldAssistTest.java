package pl.greenpath.mockito.ide.refactoring.quickassist.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import pl.greenpath.mockito.ide.refactoring.TestUtils;
import pl.greenpath.mockito.ide.refactoring.ast.BindingFinder;

@RunWith(MockitoJUnitRunner.class)
public class CreateSpyFromAssignmentToFieldAssistTest {

    @Mock
    private IInvocationContext context;

    @Mock
    private CompilationUnit astRoot;

    @Mock
    private BindingFinder bindingFinderMock;

    @Mock
    private IBinding bindingMock;

    @Before
    public void before() {
        when(bindingFinderMock.resolveBinding(any(SimpleName.class))).thenReturn(bindingMock);
        when(bindingMock.getKind()).thenReturn(IBinding.VARIABLE);
    }

    @Test
    public void shouldBeApplicableForAssignmentToField() {
        when(context.getASTRoot()).thenReturn(astRoot);
        final Assignment assignment = TestUtils.createAssignment("Object", "fooField", "tospy"); 
        when(context.getCoveringNode()).thenReturn(assignment.getRightHandSide());
        final CreateSpyFromAssignmentToFieldAssist testedClass = new CreateSpyFromAssignmentToFieldAssist(
                bindingFinderMock);
        assertThat(testedClass.isApplicable(context)).isTrue();
    }

}
