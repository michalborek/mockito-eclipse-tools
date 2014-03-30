package pl.greenpath.mockito.ide.refactoring.builder;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
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

    private FieldDeclarationBuilder fieldDeclarationBuilder;

    private TypeDeclaration type;

    private final AST ast = TestUtils.AST_INSTANCE;

    @Before
    public void before() throws JavaModelException {
        rewrite = ASTRewrite.create(TestUtils.AST_INSTANCE);
        importRewrite = ImportRewrite.create(cu, false);
        final VariableDeclarationFragment fragment = TestUtils.createVariableDeclaration("type");
        TestUtils.putVariableIntoStubStatement(fragment);

        fieldDeclarationBuilder = new FieldDeclarationBuilder(fragment.getName(), rewrite, importRewrite);
        type = getTypeDeclaration(fragment);
    }

    @Test
    public void shouldConvertToFieldMock() {
        checkConversionToField("Mock");
    }

    @Test
    public void shouldConvertToFieldMockWithModifiers() {
        checkConversionToField("Mock", ModifierKeyword.FINAL_KEYWORD, ModifierKeyword.PRIVATE_KEYWORD);
    }

    @Test
    public void shouldConvertToSpyMock() {
        checkConversionToField("Spy");
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void shouldConvertToMockWithExtraInterfaces() {
        final TypeLiteral typeLiteral = ast.newTypeLiteral();
        typeLiteral.setType(ast.newSimpleType(ast.newSimpleName("List")));
        fieldDeclarationBuilder.setAnnotationWithExtraInterfaces("org.mockito.Mock", typeLiteral);
        fieldDeclarationBuilder.build();

        final List typeRewrittenList = rewrite.getListRewrite(type, type.getBodyDeclarationsProperty())
                .getRewrittenList();
        final FieldDeclaration fieldDeclaration = (FieldDeclaration) typeRewrittenList.get(0);
        final List fieldModifiers = rewrite.getListRewrite(fieldDeclaration, FieldDeclaration.MODIFIERS2_PROPERTY)
                .getRewrittenList();
        assertThat(fieldModifiers.get(0)).isInstanceOf(NormalAnnotation.class);

        final NormalAnnotation fieldModifier = (NormalAnnotation) fieldModifiers.get(0);

        assertThat(fieldModifier.toString()).isEqualTo("@Mock(extraInterfaces={List.class})");
    }

    @SuppressWarnings({ "rawtypes" })
    private void checkConversionToField(final String mockAnnotation, final ModifierKeyword... modifiers) {
        fieldDeclarationBuilder.setMarkerAnnotation("org.mockito." + mockAnnotation).setModifiers(modifiers).build();

        final List typeRewrittenList = rewrite.getListRewrite(type, type.getBodyDeclarationsProperty())
                .getRewrittenList();
        final FieldDeclaration fieldDeclaration = (FieldDeclaration) typeRewrittenList.get(0);
        final List fieldModifiers = rewrite.getListRewrite(fieldDeclaration, FieldDeclaration.MODIFIERS2_PROPERTY)
                .getRewrittenList();
        final MarkerAnnotation fieldModifier = (MarkerAnnotation) fieldModifiers.get(0);

        for (int i = 0; i < modifiers.length; i++) {
            assertThat(((Modifier) fieldDeclaration.modifiers().get(i)).getKeyword()).isEqualTo(modifiers[i]);
        }
        assertThat(fieldModifier.getTypeName().getFullyQualifiedName()).isEqualTo(mockAnnotation);
        assertThat(importRewrite.getAddedImports()).contains("org.mockito." + mockAnnotation);
    }

    private TypeDeclaration getTypeDeclaration(final VariableDeclarationFragment fragment) {
        return new AstResolver().findParentOfType(fragment.getName(), TypeDeclaration.class);
    }
}
