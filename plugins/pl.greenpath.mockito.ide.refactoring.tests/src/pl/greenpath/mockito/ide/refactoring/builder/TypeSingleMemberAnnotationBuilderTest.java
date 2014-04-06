package pl.greenpath.mockito.ide.refactoring.builder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static pl.greenpath.mockito.ide.refactoring.TestUtils.AST_INSTANCE;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TypeSingleMemberAnnotationBuilderTest {

    private CompilationUnit astRoot;

    private ASTRewrite rewrite;

    @Mock
    private ICompilationUnit cu;

    private ImportRewrite importRewrite;

    @Before
    public void before() {
        rewrite = ASTRewrite.create(AST_INSTANCE);
        astRoot = spy(AST_INSTANCE.newCompilationUnit());
        when(astRoot.getTypeRoot()).thenReturn(cu);
        importRewrite = ImportRewrite.create(astRoot, true);

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void shouldAddRunWithIfOtherSingleMemberAnnotationExists() {
        final TypeDeclaration typeDeclaration = getClassWithSingleMemberAnnotation("FooBar", "FooAnnotation",
                "FooClass");

        new TypeSingleMemberAnnotationBuilder(typeDeclaration, astRoot, rewrite, importRewrite)
                .setQualifiedName("RunWith").setValue("MockitoJunitRunner").build();
        final List rewrittenList = rewrite.getListRewrite(typeDeclaration, TypeDeclaration.MODIFIERS2_PROPERTY)
                .getRewrittenList();

        assertThat(rewrittenList).hasSize(2);
        assertThat(rewrittenList.get(0)).isInstanceOf(SingleMemberAnnotation.class);
        final SingleMemberAnnotation addedAnnotation = (SingleMemberAnnotation) rewrittenList.get(0);
        assertThat(addedAnnotation.getTypeName().getFullyQualifiedName()).isEqualTo("RunWith");
        final Type type = ((TypeLiteral) addedAnnotation.getValue()).getType();
        assertThat(type).isInstanceOf(SimpleType.class);
        assertThat(((SimpleType) type).getName().getFullyQualifiedName()).isEqualTo("MockitoJunitRunner");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void shouldAddRunWithIfOtherAnnotationExists() {
        final TypeDeclaration typeDeclaration = getClassWithMarkerAnnotation("FooBar", "FooAnnotation");

        new TypeSingleMemberAnnotationBuilder(typeDeclaration, astRoot, rewrite, importRewrite)
                .setQualifiedName("RunWith").setValue("MockitoJunitRunner").build();
        final List rewrittenList = rewrite.getListRewrite(typeDeclaration, TypeDeclaration.MODIFIERS2_PROPERTY)
                .getRewrittenList();

        assertThat(rewrittenList).hasSize(2);
        assertThat(rewrittenList.get(0)).isInstanceOf(SingleMemberAnnotation.class);
        final SingleMemberAnnotation addedAnnotation = (SingleMemberAnnotation) rewrittenList.get(0);
        assertThat(addedAnnotation.getTypeName().getFullyQualifiedName()).isEqualTo("RunWith");
        final Type type = ((TypeLiteral) addedAnnotation.getValue()).getType();
        assertThat(type).isInstanceOf(SimpleType.class);
        assertThat(((SimpleType) type).getName().getFullyQualifiedName()).isEqualTo("MockitoJunitRunner");
    }

    @SuppressWarnings({ "rawtypes" })
    @Test
    public void shouldNotAddRunWithIfAlreadyExists() {
        final TypeDeclaration typeDeclaration = getClassWithSingleMemberAnnotation("FooBar", "RunWith",
                "MockitoJunitRunner");

        new TypeSingleMemberAnnotationBuilder(typeDeclaration, astRoot, rewrite, importRewrite)
                .setQualifiedName("RunWith").setValue("MockitoJunitRunner").build();

        final List originalList = rewrite.getListRewrite(typeDeclaration, TypeDeclaration.MODIFIERS2_PROPERTY)
                .getOriginalList();
        final List rewrittenList = rewrite.getListRewrite(typeDeclaration, TypeDeclaration.MODIFIERS2_PROPERTY)
                .getRewrittenList();
        assertThat(rewrittenList.get(0)).isSameAs(originalList.get(0));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private TypeDeclaration getClassWithSingleMemberAnnotation(final String className, final String annotationName,
            final String annotationArgumentClassName) {
        final TypeDeclaration typeDeclaration = AST_INSTANCE.newTypeDeclaration();
        typeDeclaration.setName(AST_INSTANCE.newSimpleName(className));

        final List structuralProperty = ((List) typeDeclaration.getStructuralProperty(typeDeclaration
                .getModifiersProperty()));
        final SingleMemberAnnotation annotation = AST_INSTANCE.newSingleMemberAnnotation();
        final TypeLiteral typeLiteral = AST_INSTANCE.newTypeLiteral();
        typeLiteral.setType(AST_INSTANCE.newSimpleType(AST_INSTANCE.newSimpleName(annotationArgumentClassName)));
        annotation.setTypeName(AST_INSTANCE.newSimpleName(annotationName));
        annotation.setValue(typeLiteral);
        structuralProperty.add(annotation);
        return typeDeclaration;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private TypeDeclaration getClassWithMarkerAnnotation(final String className, final String annotationName) {
        final TypeDeclaration typeDeclaration = AST_INSTANCE.newTypeDeclaration();
        typeDeclaration.setName(AST_INSTANCE.newSimpleName(className));

        final List structuralProperty = ((List) typeDeclaration.getStructuralProperty(typeDeclaration
                .getModifiersProperty()));
        final MarkerAnnotation annotation = AST_INSTANCE.newMarkerAnnotation();
        annotation.setTypeName(AST_INSTANCE.newSimpleName(annotationName));
        structuralProperty.add(annotation);
        return typeDeclaration;
    }

}
