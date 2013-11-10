package pl.greenpath.mockito.ide.refactoring.builder;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;

public class TypeSingleMemberAnnotationBuilder {

	private final CompilationUnit astRoot;
	private final ImportRewrite importRewrite;
	private final ITypeBinding typeBinding;
	private final ASTRewrite rewrite;
	private final SingleMemberAnnotation annotation;
	private final AST ast;

	public TypeSingleMemberAnnotationBuilder(final ITypeBinding typeBinding, final CompilationUnit astRoot,
			final ASTRewrite rewrite, final ImportRewrite importRewrite) {
		this.astRoot = astRoot;
		this.rewrite = rewrite;
		this.typeBinding = typeBinding;
		this.importRewrite = importRewrite;
		ast = astRoot.getAST();
		annotation = ast.newSingleMemberAnnotation();
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
		final ContextSensitiveImportRewriteContext context = new ContextSensitiveImportRewriteContext(astRoot, importRewrite);
		return importRewrite.addImport(importDefinition, context);
	}

	public void build() {
		if (!containsAnnotation(annotation)) {
			final ASTNode typeNode = astRoot.findDeclaringNode(typeBinding);
			rewrite.getListRewrite(typeNode, TypeDeclaration.MODIFIERS2_PROPERTY)
					.insertFirst(annotation, null);
		}
	}

	private boolean containsAnnotation(final SingleMemberAnnotation annotation) {
		// TODO what about complex refactorings?
		for (final IAnnotationBinding node : typeBinding.getAnnotations()) {
			if (node.getName().equals(annotation.getTypeName().getFullyQualifiedName())) {
				return true;
			}
		}
		return false;
	}

}
