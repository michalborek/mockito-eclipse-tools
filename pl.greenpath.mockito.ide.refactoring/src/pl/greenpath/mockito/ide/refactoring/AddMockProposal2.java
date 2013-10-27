package pl.greenpath.mockito.ide.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;

@SuppressWarnings("restriction")
public class AddMockProposal2 extends ASTRewriteCorrectionProposal {

	private final ITypeBinding typeBinding;
	private final SimpleName selectedNode;
	private final CompilationUnit astRoot;
	private final AstResolver astResolver;
	private final BindingFinder bindingFinder;

	public AddMockProposal2(String name, ICompilationUnit cu, SimpleName selectedNode, ITypeBinding typeBinding,
			CompilationUnit astRoot) {
		super(name, cu, null, 0);
		this.selectedNode = selectedNode;
		this.typeBinding = typeBinding;
		this.astRoot = astRoot;
		astResolver = new AstResolver();
		bindingFinder = new BindingFinder();
	}

	@Override
	protected ASTRewrite getRewrite() throws CoreException {
		return createRewrite(createFieldDeclaration(createType()));
	}

	private ASTRewrite createRewrite(FieldDeclaration fieldDeclaration) {
		ITypeBinding selectedNodeTypeBinding = bindingFinder.getParentTypeBinding(selectedNode);
		ASTNode declaringNode = astRoot.findDeclaringNode(selectedNodeTypeBinding);
		ChildListPropertyDescriptor property = astResolver.getBodyDeclarationsProperty(declaringNode);

		ASTRewrite rewrite = ASTRewrite.create(selectedNode.getAST());
		rewrite.getListRewrite(declaringNode, property).insertFirst(fieldDeclaration, null);
		return rewrite;
	}

	@SuppressWarnings("restriction")
	private Type createType() {
		ImportRewrite imports = createImportRewrite(astRoot);
		// TODO stop using ContextSensitiveImportRewriteContext since it's
		// internal
		ImportRewriteContext importRewriteContext = new ContextSensitiveImportRewriteContext(
				astResolver.findParentBodyDeclaration(selectedNode), imports);
		return imports.addImport(this.typeBinding, selectedNode.getAST(), importRewriteContext);
	}

	@SuppressWarnings("unchecked")
	private FieldDeclaration createFieldDeclaration(Type newType) {
		AST ast = selectedNode.getAST();
		VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
		fragment.setName(ast.newSimpleName(selectedNode.getIdentifier()));

		FieldDeclaration fieldDeclaration = ast.newFieldDeclaration(fragment);
		fieldDeclaration.setType(newType);
		fieldDeclaration.modifiers().add(ast.newModifier(ModifierKeyword.PRIVATE_KEYWORD));
		return fieldDeclaration;
	}

}
