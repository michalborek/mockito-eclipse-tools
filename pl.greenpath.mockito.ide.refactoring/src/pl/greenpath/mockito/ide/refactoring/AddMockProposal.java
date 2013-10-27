package pl.greenpath.mockito.ide.refactoring;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.internal.ui.text.correction.ASTResolving;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public class AddMockProposal implements IJavaCompletionProposal {

	private final IInvocationContext invocationContext;
	private final IProblemLocation location;

	public AddMockProposal(IInvocationContext context, IProblemLocation location) {
		this.invocationContext = context;
		this.location = location;
	}

	@Override
	public int getRelevance() {
		return 0;
	}

	@Override
	public void apply(IDocument document) {
		CompilationUnit root = invocationContext.getASTRoot();
		SimpleName coveredNode = (SimpleName) location.getCoveredNode(root);
		ITypeBinding typeBinding = ASTResolving.guessBindingForReference(coveredNode);

		IType type = invocationContext.getCompilationUnit().findPrimaryType();
		try {
			
			type.createField("\n@Mock\n" + 
					typeBinding.getName() + " " + coveredNode.getIdentifier() + 
					";", null, false, new NullProgressMonitor());
			invocationContext.getCompilationUnit().createImport("org.mockito.Mock", null, new NullProgressMonitor());
			invocationContext.getCompilationUnit().createImport(typeBinding.getBinaryName(), null, new NullProgressMonitor());
			AST ast = root.getAST();
			AnnotationTypeDeclaration newAnnotation = ast.newAnnotationTypeDeclaration();
			newAnnotation.setName(ast.newSimpleName("MockitoJunitRunner.class"));
			
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Point getSelection(IDocument document) {
		return null;
	}

	@Override
	public String getAdditionalProposalInfo() {
		return null;
	}

	@Override
	public String getDisplayString() {
		return "Add mock";
	}

	@Override
	public Image getImage() {
		return null;
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

}