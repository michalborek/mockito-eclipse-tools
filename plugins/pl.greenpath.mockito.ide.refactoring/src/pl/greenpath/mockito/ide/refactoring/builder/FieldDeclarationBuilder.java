package pl.greenpath.mockito.ide.refactoring.builder;

import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;

import pl.greenpath.mockito.ide.refactoring.ast.AstResolver;
import pl.greenpath.mockito.ide.refactoring.ast.BindingFinder;

public class FieldDeclarationBuilder {

    private final ASTRewrite rewrite;
    private final AST ast;
    private final FieldDeclaration fieldDeclaration;
    private final ImportRewrite importRewrite;
    private final SimpleName selectedNode;
    private final AstResolver astResolver;
    private final BindingFinder bindingFinder;
    private final CompilationUnit parentClass;
    private final ImportRewriteContext importRewriteContext;
    private Annotation annotation;

    public FieldDeclarationBuilder(final SimpleName variableName, final CompilationUnit parentClass,
            final ASTRewrite rewrite,
            final ImportRewrite importRewrite) {
        this.parentClass = parentClass;
        ast = variableName.getAST();
        this.selectedNode = variableName;
        this.rewrite = rewrite;
        this.importRewrite = importRewrite;
        astResolver = new AstResolver();
        bindingFinder = new BindingFinder();
        fieldDeclaration = createFieldDeclaration();
        importRewriteContext = new ContextSensitiveImportRewriteContext(variableName, importRewrite);
    }

    public void build() {
        final ASTNode declaringNode = parentClass.findDeclaringNode(bindingFinder.getParentTypeBinding(selectedNode));
        insertField(rewrite.getListRewrite(declaringNode, astResolver.getBodyDeclarationsProperty(declaringNode)));
        rewrite.getListRewrite(fieldDeclaration, FieldDeclaration.MODIFIERS2_PROPERTY).insertFirst(annotation, null);
    }

    private void insertField(final ListRewrite fields) {
        final FieldDeclaration insertAfter = getInsertAfterItem(fields.getRewrittenList());
        if (insertAfter == null) {
            fields.insertFirst(fieldDeclaration, null);
        } else {
            fields.insertAfter(fieldDeclaration, insertAfter, null);
        }
    }

    @SuppressWarnings("rawtypes")
    private FieldDeclaration getInsertAfterItem(final List fields) {
        FieldDeclaration insertBefore = null;
        for (final Object fieldObj : fields) {
            if (!(fieldObj instanceof FieldDeclaration)) {
                continue;
            }
            final FieldDeclaration field = (FieldDeclaration) fieldObj;

            final List modifiers = (List) field.getStructuralProperty(field.getModifiersProperty());
            boolean found = false;
            for (final Object modifier : modifiers) {
                if (modifier instanceof Annotation
                        && ((Annotation) modifier).getTypeName().getFullyQualifiedName().equals("Mock")) {
                    insertBefore = field;
                    found = true;
                    break;
                }
            }
            if (!found && insertBefore != null) {
                break;
            }
        }
        return insertBefore;
    }

    private FieldDeclaration createFieldDeclaration() {
        final VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
        fragment.setName(ast.newSimpleName(selectedNode.getIdentifier()));
        return ast.newFieldDeclaration(fragment);
    }

    public FieldDeclarationBuilder setType(final ITypeBinding typeBinding) {
        final Type type = importRewrite.addImport(typeBinding, selectedNode.getAST(), importRewriteContext);
        fieldDeclaration.setType(type);
        return this;
    }

    @SuppressWarnings("unchecked")
    public FieldDeclarationBuilder setModifiers(final ModifierKeyword... modifiers) {
        for (final ModifierKeyword modifierKeyword : modifiers) {
            fieldDeclaration.modifiers().add(ast.newModifier(modifierKeyword));
        }
        return this;
    }

    public FieldDeclarationBuilder setMarkerAnnotation(final String fullyQualifiedName) {
        annotation = ast.newMarkerAnnotation();
        annotation.setTypeName(ast.newSimpleName(importType(fullyQualifiedName)));
        return this;
    }

    private String importType(final String qualifiedName) {
        return importRewrite.addImport(qualifiedName, importRewriteContext);
    }

    @SuppressWarnings("unchecked")
    public void setAnnotationWithExtraInterfaces(final String fullyQualifiedName, final TypeLiteral... types) {
        final NormalAnnotation normalAnnotation = ast.newNormalAnnotation();
        
        normalAnnotation.setTypeName(ast.newSimpleName(importType(fullyQualifiedName)));
        final MemberValuePair newMemberValuePair = ast.newMemberValuePair();
        newMemberValuePair.setName(ast.newSimpleName("extraInterfaces"));
        final ArrayInitializer newArrayInitializer = ast.newArrayInitializer();
        for(final TypeLiteral literal: types) {
            newArrayInitializer.expressions().add(ASTNode.copySubtree(ast, literal));
        }
        newMemberValuePair.setValue(newArrayInitializer);
        normalAnnotation.values().add(newMemberValuePair);
        annotation = normalAnnotation;
    }
}
