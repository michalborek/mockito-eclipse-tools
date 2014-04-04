package pl.greenpath.mockito.ide.refactoring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class ConversionsChecker {

    @SuppressWarnings("rawtypes")
    public static void checkIfFieldWithMockHasBeenAdded(final ASTRewrite rewrite, final TypeDeclaration typeDeclaration, final String mockAttributeName, final String className) {
        final List bodyDeclarations = rewrite.getListRewrite(typeDeclaration,
                TypeDeclaration.BODY_DECLARATIONS_PROPERTY).getRewrittenList();
        assertThat(bodyDeclarations.get(0)).isInstanceOf(FieldDeclaration.class);
        final FieldDeclaration field = (FieldDeclaration) bodyDeclarations.get(0);
        assertThat(((SimpleType) field.getType()).getName().getFullyQualifiedName()).isEqualTo(className);
        assertThat(((Modifier) field.modifiers().get(0)).getKeyword()).isEqualTo(ModifierKeyword.PRIVATE_KEYWORD);
        assertThat(((VariableDeclarationFragment) field.fragments().get(0)).getName().getFullyQualifiedName())
                .isEqualTo(mockAttributeName);
        final List fieldModifiers = rewrite.getListRewrite(field, FieldDeclaration.MODIFIERS2_PROPERTY)
                .getRewrittenList();
        assertThat(((MarkerAnnotation) fieldModifiers.get(0)).getTypeName().getFullyQualifiedName()).isEqualTo("Mock");
    }

    @SuppressWarnings("rawtypes")
    public static void checkIfRunWithAnnotationIsProperlyAdded(final ASTRewrite rewrite, final TypeDeclaration typeDeclaration) {
        final List rewrittenModifiers = rewrite.getListRewrite(typeDeclaration, TypeDeclaration.MODIFIERS2_PROPERTY)
                .getRewrittenList();
        assertThat(rewrittenModifiers.get(0)).isInstanceOf(SingleMemberAnnotation.class);
        final SingleMemberAnnotation addedAnnotation = (SingleMemberAnnotation) rewrittenModifiers.get(0);
        assertThat(addedAnnotation.getTypeName().getFullyQualifiedName()).isEqualTo("RunWith");
        assertThat(addedAnnotation.getValue()).isInstanceOf(TypeLiteral.class);
        final TypeLiteral parameter = (TypeLiteral) addedAnnotation.getValue();
        assertThat(((SimpleType) parameter.getType()).getName().getFullyQualifiedName())
                .isEqualTo("MockitoJUnitRunner");
    }

}
