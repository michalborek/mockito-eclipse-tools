package pl.greenpath.mockito.ide.refactoring.proposal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;
import org.eclipse.swt.graphics.Image;

import pl.greenpath.mockito.ide.refactoring.PluginImages;
import pl.greenpath.mockito.ide.refactoring.ast.AstResolver;
import pl.greenpath.mockito.ide.refactoring.builder.LocalVariableDeclarationBuilder;

public class AddLocalMockProposal extends ASTRewriteCorrectionProposal {

	private static final String MOCK = "org.mockito.Mock";
	private final ITypeBinding typeBinding;
	private final SimpleName selectedNode;
	private final CompilationUnit astRoot;
	private final BodyDeclaration methodBodyDeclaration;

	public AddLocalMockProposal(final ICompilationUnit cu, final SimpleName selectedNode, final ITypeBinding typeBinding,
			final CompilationUnit astRoot) {
		super("Create local mock", cu, null, 0);
		this.selectedNode = selectedNode;
		this.typeBinding = typeBinding;
		this.astRoot = astRoot;
		methodBodyDeclaration = new AstResolver().findParentBodyDeclaration(selectedNode);
	}

	@Override
	protected ASTRewrite getRewrite() throws CoreException {
		final ASTRewrite rewrite = ASTRewrite.create(selectedNode.getAST());
		createImportRewrite(astRoot);
		addMissingVariableDeclaration(rewrite);
		return rewrite;
	}

	private void addMissingVariableDeclaration(final ASTRewrite rewrite) {
	    new LocalVariableDeclarationBuilder(selectedNode, methodBodyDeclaration, astRoot, rewrite, getImportRewrite()).
	        withMethodInvocation(typeBinding, 
	                MOCK).build();
//		new FieldDeclarationBuilder(selectedNode, methodBodyDeclaration, astRoot, rewrite, getImportRewrite())
//				.withType(typeBinding)
//				.withModifiers(ModifierKeyword.PRIVATE_KEYWORD)
//				.withMarkerAnnotation(MOCK)
//				.build();
	    // TODO
	}

	@Override
	public int getRelevance() {
		if (selectedNode.getIdentifier().toLowerCase().endsWith("mock")) {
			return 99;
		}
		return super.getRelevance();
	}
	
	@Override
	public Image getImage() {
		return PluginImages.get(ISharedImages.IMG_FIELD_PRIVATE);
	}

}
