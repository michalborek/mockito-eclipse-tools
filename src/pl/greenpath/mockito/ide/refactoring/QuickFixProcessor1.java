package pl.greenpath.mockito.ide.refactoring;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;

public class QuickFixProcessor1 implements IQuickFixProcessor {

	private final BindingFinder bindingFinder = new BindingFinder(); // TODO dependency injection

	@Override
	public IJavaCompletionProposal[] getCorrections(IInvocationContext context, IProblemLocation[] locations)
			throws CoreException {
		List<IJavaCompletionProposal> corrections = new ArrayList<IJavaCompletionProposal>();

		for (IProblemLocation location : locations) {
			if (hasCorrections(context.getCompilationUnit(), location.getProblemId())) {
				corrections.add(getAddMockProposal(context, location));
			}
		}
		return corrections.toArray(new IJavaCompletionProposal[0]);
	}

	private AddMockProposal2 getAddMockProposal(IInvocationContext context, IProblemLocation location) {
		ICompilationUnit compilationUnit = context.getCompilationUnit();
		CompilationUnit astRoot = context.getASTRoot();

		ASTNode selectedNode = location.getCoveredNode(astRoot);

		ITypeBinding binding = bindingFinder.getParentTypeBinding(selectedNode);
		ITypeBinding type = null;
		
		if(selectedNode.getNodeType() == ASTNode.SIMPLE_NAME) {
			StructuralPropertyDescriptor locationInParent = selectedNode.getLocationInParent();
			ASTNode parent = selectedNode.getParent();
			int index = ((List)selectedNode.getParent()
					.getStructuralProperty(locationInParent)).indexOf(selectedNode);
			if(parent.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
				type = getProposedType(parent, index);
			}
			return new AddMockProposal2("Dodanie Mock", compilationUnit, (SimpleName)selectedNode, type, astRoot);
		}
		
		return null;
	}

	private ITypeBinding getProposedType(ASTNode parent, int index) {
		ClassInstanceCreation parentNode = (ClassInstanceCreation) parent;
		IMethodBinding constructorBinding = parentNode.resolveConstructorBinding();
		return constructorBinding.getParameterTypes()[index];
	}

	@Override
	public boolean hasCorrections(ICompilationUnit unit, int problemId) {
		if (problemId == IProblem.UnresolvedVariable) {
			return unit.findPrimaryType().getElementName().endsWith("Test");
		}
		return false;
	}

}
