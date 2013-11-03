package pl.greenpath.mockito.ide.refactoring.quickfix;

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

import pl.greenpath.mockito.ide.refactoring.proposal.AddMockFieldProposal;

public class MocksQuickFixProcessor implements IQuickFixProcessor {

	@Override
	public IJavaCompletionProposal[] getCorrections(final IInvocationContext context, final IProblemLocation[] locations)
			throws CoreException {
		final List<IJavaCompletionProposal> corrections = new ArrayList<IJavaCompletionProposal>();

		for (final IProblemLocation location : locations) {
			if (hasCorrections(context.getCompilationUnit(), location.getProblemId())) {
				final AddMockFieldProposal proposal = getAddMockProposal(context, location);
				if (proposal != null) {
					corrections.add(proposal);
				}
			}
		}
		return corrections.toArray(new IJavaCompletionProposal[0]);
	}

	private AddMockFieldProposal getAddMockProposal(final IInvocationContext context, final IProblemLocation location) {
		final ICompilationUnit compilationUnit = context.getCompilationUnit();
		final CompilationUnit astRoot = context.getASTRoot();
		final ASTNode selectedNode = location.getCoveredNode(astRoot);

		if (selectedNode.getNodeType() == ASTNode.SIMPLE_NAME) {
			final StructuralPropertyDescriptor locationInParent = selectedNode.getLocationInParent();
			final ASTNode parent = selectedNode.getParent();
			final int index = ((List) selectedNode.getParent()
					.getStructuralProperty(locationInParent)).indexOf(selectedNode);
			if (parent.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
				final ITypeBinding type = getProposedType(parent, index);
				return new AddMockFieldProposal(compilationUnit, (SimpleName) selectedNode, type, astRoot);
			}
		}

		return null;
	}

	private ITypeBinding getProposedType(final ASTNode parent, final int index) {
		final ClassInstanceCreation parentNode = (ClassInstanceCreation) parent;
		final IMethodBinding constructorBinding = parentNode.resolveConstructorBinding();
		return constructorBinding.getParameterTypes()[index];
	}

	@Override
	public boolean hasCorrections(final ICompilationUnit unit, final int problemId) {
		return problemId == IProblem.UnresolvedVariable
				&& unit.findPrimaryType().getElementName().endsWith("Test");
	}

}
