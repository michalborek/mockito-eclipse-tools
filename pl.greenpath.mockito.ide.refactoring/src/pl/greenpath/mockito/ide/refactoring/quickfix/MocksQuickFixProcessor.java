package pl.greenpath.mockito.ide.refactoring.quickfix;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;

import pl.greenpath.mockito.ide.refactoring.proposal.AddMockFieldProposal;
import pl.greenpath.mockito.ide.refactoring.quickfix.exception.NotSupportedRefactoring;

public class MocksQuickFixProcessor implements IQuickFixProcessor {

	@Override
	public IJavaCompletionProposal[] getCorrections(final IInvocationContext context, final IProblemLocation[] locations)
			throws CoreException {
		final List<IJavaCompletionProposal> corrections = new ArrayList<IJavaCompletionProposal>();

		for (final IProblemLocation location : locations) {
			if (hasCorrections(context.getCompilationUnit(), location.getProblemId())) {
				try {
					corrections.add(getAddMockProposal(context, location));
				} catch (final NotSupportedRefactoring e) {
					// TODO logging
					e.printStackTrace();
				}
			}
		}
		return corrections.toArray(new IJavaCompletionProposal[0]);
	}

	private AddMockFieldProposal getAddMockProposal(final IInvocationContext context, final IProblemLocation location)
			throws NotSupportedRefactoring {
		final ASTNode selectedNode = location.getCoveredNode(context.getASTRoot());
		if (selectedNode.getNodeType() != ASTNode.SIMPLE_NAME) {
			throw new NotSupportedRefactoring("This selection is not supported by this refactoring");
		}
		return new AddMockFieldProposal(context.getCompilationUnit(), (SimpleName) selectedNode,
				getProposedType(selectedNode), context.getASTRoot());
	}

	private ITypeBinding getProposedType(final ASTNode selectedNode) throws NotSupportedRefactoring {
		final ASTNode parent = selectedNode.getParent();
		final int index = getParentMembers(selectedNode).indexOf(selectedNode);
		if (parent.getNodeType() == ASTNode.CLASS_INSTANCE_CREATION) {
			return getProposedTypeFromConstructor(index, (ClassInstanceCreation) parent);
		} else if (parent.getNodeType() == ASTNode.METHOD_INVOCATION) {
			return getProposedTypeFromMethodInvocation(index, (MethodInvocation) parent);
		}
		throw new NotSupportedRefactoring("Type of invocation not supported by this fix processor: "
				+ parent.getClass().getName());
	}

	@SuppressWarnings("rawtypes")
	private List getParentMembers(final ASTNode selectedNode) {
		return (List) selectedNode.getParent().getStructuralProperty(selectedNode.getLocationInParent());
	}

	private ITypeBinding getProposedTypeFromMethodInvocation(final int index, final MethodInvocation parent) {
		return getTypeFromBinding(index, parent.resolveMethodBinding());
	}

	private ITypeBinding getProposedTypeFromConstructor(final int index, final ClassInstanceCreation parent) {
		return getTypeFromBinding(index, parent.resolveConstructorBinding());
	}

	private ITypeBinding getTypeFromBinding(final int index, final IMethodBinding binding) {
		return binding.getParameterTypes()[index];
	}

	@Override
	public boolean hasCorrections(final ICompilationUnit unit, final int problemId) {
		return problemId == IProblem.UnresolvedVariable
				&& unit.findPrimaryType().getElementName().endsWith("Test");
	}

}
