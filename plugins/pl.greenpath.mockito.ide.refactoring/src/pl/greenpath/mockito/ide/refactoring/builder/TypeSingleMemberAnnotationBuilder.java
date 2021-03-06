package pl.greenpath.mockito.ide.refactoring.builder;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;

public class TypeSingleMemberAnnotationBuilder {

    private final ImportRewrite importRewrite;
    private final TypeDeclaration type;
    private final ASTRewrite rewrite;
    private final SingleMemberAnnotation annotation;
    private final AST ast;

    public TypeSingleMemberAnnotationBuilder(final TypeDeclaration typeDeclaration, final CompilationUnit astRoot,
            final ASTRewrite rewrite, final ImportRewrite importRewrite) {
        this.rewrite = rewrite;
        this.type = typeDeclaration;
        this.importRewrite = importRewrite;
        this.ast = astRoot.getAST();
        this.annotation = ast.newSingleMemberAnnotation();
    }

    public TypeSingleMemberAnnotationBuilder setQualifiedName(final String qualifiedName) {
        annotation.setTypeName(ast.newName(addImport(qualifiedName)));
        return this;
    }

    public TypeSingleMemberAnnotationBuilder setValue(final String value) {
        final TypeLiteral member = ast.newTypeLiteral();
        member.setType(ast.newSimpleType(ast.newSimpleName(addImport(value))));
        annotation.setValue(member);
        return this;
    }

    private String addImport(final String importDefinition) {
        return importRewrite.addImport(importDefinition);
    }

    public void build() {
        if (!containsAnnotation(annotation)) {
            rewrite.getListRewrite(type, TypeDeclaration.MODIFIERS2_PROPERTY).insertFirst(annotation, null);
        }
    }

    private boolean containsAnnotation(final SingleMemberAnnotation annotation) {
        // TODO what about complex refactorings?
        for (final Object modifier : type.modifiers()) {
            if (modifier instanceof SingleMemberAnnotation) {
                final SingleMemberAnnotation existingAnnotation = (SingleMemberAnnotation) modifier;
                if (existingAnnotation.getTypeName().getFullyQualifiedName()
                        .equals(annotation.getTypeName().getFullyQualifiedName())) {
                    return true;
                }
            }
        }
        return false;
    }

}
