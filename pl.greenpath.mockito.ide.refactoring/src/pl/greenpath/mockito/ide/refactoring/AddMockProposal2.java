package pl.greenpath.mockito.ide.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;

import pl.greenpath.mockito.ide.refactoring.builder.FieldDeclarationBuilder;
import pl.greenpath.mockito.ide.refactoring.builder.TypeSingleMemberAnnotationBuilder;

public class AddMockProposal2 extends ASTRewriteCorrectionProposal {

	private static final String MOCK = "org.mockito.Mock";
	private static final String MOCKITO_JUNIT_RUNNER = "org.mockito.runners.MockitoJUnitRunner";
	private static final String RUN_WITH = "org.junit.runner.RunWith";
	private final ITypeBinding typeBinding;
	private final SimpleName selectedNode;
	private final CompilationUnit astRoot;
	private final BodyDeclaration methodBodyDeclaration;

	public AddMockProposal2(String name, ICompilationUnit cu, SimpleName selectedNode, ITypeBinding typeBinding,
			CompilationUnit astRoot) {
		super(name, cu, null, 0);
		this.selectedNode = selectedNode;
		this.typeBinding = typeBinding;
		this.astRoot = astRoot;
		methodBodyDeclaration = new AstResolver().findParentBodyDeclaration(selectedNode);
	}

	@Override
	protected ASTRewrite getRewrite() throws CoreException {
		ASTRewrite rewrite = ASTRewrite.create(selectedNode.getAST());
		createImportRewrite(astRoot);
		addMissingFieldDeclaration(rewrite);
		addRunWithAnnotation(rewrite);
		return rewrite;
	}

	private void addRunWithAnnotation(ASTRewrite rewrite) {
		new TypeSingleMemberAnnotationBuilder(new BindingFinder().getParentTypeBinding(methodBodyDeclaration), astRoot,
				rewrite, getImportRewrite())
				.withQualifiedName(RUN_WITH)
				.withValue(MOCKITO_JUNIT_RUNNER)
				.build();
	}

	private void addMissingFieldDeclaration(ASTRewrite rewrite) {
		new FieldDeclarationBuilder(selectedNode, methodBodyDeclaration, astRoot, rewrite, getImportRewrite())
				.withType(typeBinding)
				.withModifiers(ModifierKeyword.PRIVATE_KEYWORD)
				.withMarkerAnnotation(MOCK)
				.build();
	}

}
