package pl.greenpath.mockito.ide.refactoring;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class BindingFinder {

	public ITypeBinding getParentTypeBinding(ASTNode node) {
		if (node == null) {
			return null;
		}
		if (node instanceof AnonymousClassDeclaration) {
			return ((AnonymousClassDeclaration) node).resolveBinding();
		}
		if (node instanceof AbstractTypeDeclaration) {
			return ((AbstractTypeDeclaration) node).resolveBinding();
		}
		return getParentTypeBinding(node.getParent());
	}
}
