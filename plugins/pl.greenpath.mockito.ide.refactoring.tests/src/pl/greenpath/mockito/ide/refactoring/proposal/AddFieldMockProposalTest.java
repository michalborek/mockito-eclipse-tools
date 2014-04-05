package pl.greenpath.mockito.ide.refactoring.proposal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import pl.greenpath.mockito.ide.refactoring.ConversionsChecker;
import pl.greenpath.mockito.ide.refactoring.TestUtils;
import pl.greenpath.mockito.ide.refactoring.ast.AstResolver;
import pl.greenpath.mockito.ide.refactoring.ast.ContextBaseTypeFinder;

@RunWith(MockitoJUnitRunner.class)
public class AddFieldMockProposalTest {

    @Mock
    private ICompilationUnit cu;
    @Mock
    private CompilationUnit astRoot;

    @Before
    public void before() {
        astRoot = spy(TestUtils.AST_INSTANCE.newCompilationUnit());
        when(astRoot.getTypeRoot()).thenReturn(cu);
    }

    @Test
    public void shouldDisplayProperDescriptionForMocks() {
        final SimpleName selectedNode = TestUtils.createVariableDeclaration("foo").getName();
        final AddFieldMockProposal testedClass = new AddFieldMockProposal(cu, selectedNode, astRoot, "Mock");
        assertThat(testedClass.getDisplayString()).isEqualTo("Create field mock");
    }

    @Test
    public void shouldDisplayProperDescriptionForSpies() {
        final SimpleName selectedNode = TestUtils.createVariableDeclaration("foo").getName();
        final AddFieldMockProposal testedClass = new AddFieldMockProposal(cu, selectedNode, astRoot, "Spy");
        assertThat(testedClass.getDisplayString()).isEqualTo("Create field spy");
    }

    @Test
    public void toStringShouldBeOverridden() {
        final SimpleName selectedNode = TestUtils.createVariableDeclaration("foo").getName();
        final AddFieldMockProposal testedClass = new AddFieldMockProposal(cu, selectedNode, astRoot, "Mock");

        assertThat(testedClass.toString()).isEqualTo("AddFieldMockProposal [selectedNode=foo, mockitoAnnotation=Mock]");
    }

    @Test
    public void shouldReturnImage() {
        final SimpleName selectedNode = TestUtils.createVariableDeclaration("foo").getName();
        final AddFieldMockProposal testedClass = new AddFieldMockProposal(cu, selectedNode, astRoot, "Mock");
        assertThat(testedClass.getImage()).isNotNull();
    }

    @Test
    public void shouldReturnBiggerRelevanceForMockEndingName() {
        final SimpleName selectedNode = TestUtils.createVariableDeclaration("foo").getName();
        final SimpleName selectedNodeWithMockEnding = TestUtils.createVariableDeclaration("fooMock").getName();
        final AddFieldMockProposal withoutMockEndingNode = new AddFieldMockProposal(cu, selectedNode, astRoot, "Mock");
        final AddFieldMockProposal withMockEndingNode = new AddFieldMockProposal(cu, selectedNodeWithMockEnding,
                astRoot, "Mock");

        assertThat(withMockEndingNode.getRelevance()).isGreaterThan(withoutMockEndingNode.getRelevance());
    }

    @Test
    public void shouldReturnRewriteWithAddedMocks() throws CoreException {
        final VariableDeclarationFragment fragment = TestUtils.createVariableDeclaration("fooMock");
        final VariableDeclarationStatement statement = TestUtils.putVariableIntoStubStatement(fragment);

        final AddFieldMockProposal testedClass = new AddFieldMockProposal(cu, fragment.getName(), astRoot, "Mock",
                getFinderMock());

        final ASTRewrite rewrite = testedClass.getRewrite();
        final TypeDeclaration typeDeclaration = new AstResolver().findParentOfType(statement, TypeDeclaration.class);
        ConversionsChecker.checkIfRunWithAnnotationIsProperlyAdded(rewrite, typeDeclaration);
        ConversionsChecker.checkIfFieldMockHasBeenAdded(rewrite, typeDeclaration, "fooMock", "FooBar");
    }



    private ContextBaseTypeFinder getFinderMock() {
        final ContextBaseTypeFinder finderMock = mock(ContextBaseTypeFinder.class);
        final ITypeBinding typeBindingMock = mock(ITypeBinding.class);
        final ITypeBinding typeDeclarationMock = mock(ITypeBinding.class);
        when(typeDeclarationMock.getQualifiedName()).thenReturn("pl.greenpath.test.FooBar");
        when(typeBindingMock.getTypeDeclaration()).thenReturn(typeDeclarationMock);
        when(typeBindingMock.getName()).thenReturn("FooBar");
        when(typeBindingMock.getTypeArguments()).thenReturn(new ITypeBinding[0]);
        when(typeBindingMock.getAnnotations()).thenReturn(new IAnnotationBinding[0]);
        when(finderMock.find((ASTNode) Mockito.any())).thenReturn(typeBindingMock);
        return finderMock;
    }

}
