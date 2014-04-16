package pl.greenpath.mockito.ide.refactoring.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
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

    private FieldDeclarationBuilder testedClass;

    private TypeDeclaration type;

    private final AST ast = TestUtils.AST_INSTANCE;

    @Before
    public void before() throws JavaModelException {
        rewrite = ASTRewrite.create(TestUtils.AST_INSTANCE);
        importRewrite = ImportRewrite.create(cu, false);
        final VariableDeclarationFragment fragment = TestUtils.createVariableDeclaration("type");
        TestUtils.putVariableIntoStubStatement(fragment);

        testedClass = new FieldDeclarationBuilder(fragment.getName(), rewrite, importRewrite);
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
        testedClass.setAnnotationWithExtraInterfaces("org.mockito.Mock", typeLiteral);
        testedClass.build();

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
        testedClass.setMarkerAnnotation("org.mockito." + mockAnnotation).setModifiers(modifiers).build();

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

    @SuppressWarnings("rawtypes")
    @Test
    public void shouldAtTheBeginningIfNoFieldWithMockAnnotationExists() {

        final FieldDeclaration fieldDeclaration = TestUtils.createFieldWithAnnotation("Object", "foo", "Spy");

        final VariableDeclarationFragment fragment = TestUtils.createVariableDeclaration("type");
        final VariableDeclarationStatement statement = TestUtils.AST_INSTANCE.newVariableDeclarationStatement(fragment);
        statement.setType(TestUtils.AST_INSTANCE.newSimpleType(TestUtils.AST_INSTANCE.newSimpleName("Object")));
        final MethodDeclaration methodDeclaration = TestUtils.createMethodDeclaration(statement);
        final TypeDeclaration parentType = TestUtils.createTypeDeclaration(fieldDeclaration, methodDeclaration);
        
        final FieldDeclarationBuilder builder = new FieldDeclarationBuilder(fragment.getName(), rewrite, importRewrite);
        builder.setMarkerAnnotation("org.mockito.Mock").setType(getTypeBindingMock()).build();
        
        final List typeRewrittenList = rewrite.getListRewrite(parentType, parentType.getBodyDeclarationsProperty())
                .getRewrittenList();
        
        final FieldDeclaration rewrittenField = (FieldDeclaration) typeRewrittenList.get(0);
        
        final Object annotation = rewrite.getListRewrite(rewrittenField, FieldDeclaration.MODIFIERS2_PROPERTY)
                .getRewrittenList().get(0);
        assertThat(annotation).isInstanceOf(MarkerAnnotation.class);
        assertThat(((SimpleName) ((MarkerAnnotation) annotation).getTypeName()).getFullyQualifiedName()).isEqualTo(
                "Mock");
        assertThat(rewrittenField.toString()).isEqualTo("TestedClass type;\n");
    }

    @SuppressWarnings("rawtypes")
    @Test
    public void shouldInsertAfterExistingFieldMock() {
        
        final FieldDeclaration fieldDeclaration = TestUtils.createFieldWithAnnotation("Object", "foo", "Mock");
        
        final VariableDeclarationFragment fragment = TestUtils.createVariableDeclaration("type");
        final VariableDeclarationStatement statement = TestUtils.AST_INSTANCE.newVariableDeclarationStatement(fragment);
        statement.setType(TestUtils.AST_INSTANCE.newSimpleType(TestUtils.AST_INSTANCE.newSimpleName("Object")));
        final MethodDeclaration methodDeclaration = TestUtils.createMethodDeclaration(statement);
        final TypeDeclaration parentType = TestUtils.createTypeDeclaration(fieldDeclaration, methodDeclaration);
        
        final FieldDeclarationBuilder builder = new FieldDeclarationBuilder(fragment.getName(), rewrite, importRewrite);
        builder.setMarkerAnnotation("org.mockito.Mock").setType(getTypeBindingMock()).build();
        
        final List typeRewrittenList = rewrite.getListRewrite(parentType, parentType.getBodyDeclarationsProperty())
                .getRewrittenList();
        
        final FieldDeclaration rewrittenField = (FieldDeclaration) typeRewrittenList.get(1);
        
        final Object annotation = rewrite.getListRewrite(rewrittenField, FieldDeclaration.MODIFIERS2_PROPERTY)
                .getRewrittenList().get(0);
        assertThat(annotation).isInstanceOf(MarkerAnnotation.class);
        assertThat(((SimpleName) ((MarkerAnnotation) annotation).getTypeName()).getFullyQualifiedName()).isEqualTo(
                "Mock");
        assertThat(rewrittenField.toString()).isEqualTo("TestedClass type;\n");
    }

    private TypeDeclaration getTypeDeclaration(final VariableDeclarationFragment fragment) {
        return new AstResolver().findParentOfType(fragment.getName(), TypeDeclaration.class);
    }

    private ITypeBinding getTypeBindingMock() {
        final ITypeBinding typeBindingMock = mock(ITypeBinding.class);
        final ITypeBinding typeDeclarationMock = mock(ITypeBinding.class);
        when(typeDeclarationMock.getQualifiedName()).thenReturn("pl.greenpath.test.TestedClass");
        when(typeBindingMock.getTypeDeclaration()).thenReturn(typeDeclarationMock);
        when(typeBindingMock.getName()).thenReturn("TestedClass");
        when(typeBindingMock.getTypeArguments()).thenReturn(new ITypeBinding[0]);
        return typeBindingMock;
    }
}
