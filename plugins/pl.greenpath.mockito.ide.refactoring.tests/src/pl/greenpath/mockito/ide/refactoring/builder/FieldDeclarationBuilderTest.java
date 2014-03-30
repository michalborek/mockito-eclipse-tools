package pl.greenpath.mockito.ide.refactoring.builder;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import pl.greenpath.mockito.ide.refactoring.TestUtils;
import pl.greenpath.mockito.ide.refactoring.ast.AstResolver;

@RunWith(MockitoJUnitRunner.class)
public class FieldDeclarationBuilderTest {

    @Mock
    private ICompilationUnit cu;

    @Mock
    private CompilationUnit astRoot;

    private ASTRewrite rewrite;

    private ImportRewrite importRewrite;

    @Before
    public void before() throws JavaModelException {
        rewrite = ASTRewrite.create(TestUtils.AST_INSTANCE);
        importRewrite = ImportRewrite.create(cu, false);
    }

    @Test
    public void shouldConvertToFieldMock() {
        checkConversionToField("Mock");
    }

    @Test
    public void shouldConvertToSpyMock() {
        checkConversionToField("Spy");
    }

    @SuppressWarnings("rawtypes")
    private void checkConversionToField(final String mockAnnotation) {
        final VariableDeclarationFragment fragment = TestUtils.createVariableDeclaration("type");
        TestUtils.putVariableIntoStubStatement(fragment);

        new FieldDeclarationBuilder(fragment.getName(), rewrite, importRewrite).setMarkerAnnotation(
                "org.mockito." + mockAnnotation).build();

        final TypeDeclaration type = getTypeDeclaration(fragment);
        final List typeRewrittenList = rewrite.getListRewrite(type, type.getBodyDeclarationsProperty())
                .getRewrittenList();
        final List fieldModifiers = rewrite.getListRewrite((FieldDeclaration) typeRewrittenList.get(0),
                FieldDeclaration.MODIFIERS2_PROPERTY).getRewrittenList();
        final MarkerAnnotation fieldModifier = (MarkerAnnotation) fieldModifiers.get(0);

        assertThat(fieldModifier.getTypeName().getFullyQualifiedName()).isEqualTo(mockAnnotation);
        assertThat(importRewrite.getAddedImports()).contains("org.mockito." + mockAnnotation);
    }

    private TypeDeclaration getTypeDeclaration(final VariableDeclarationFragment fragment) {
        return new AstResolver().findParentOfType(fragment.getName(), TypeDeclaration.class);
    }
}
