package pl.greenpath.mockito.ide.refactoring.ast;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;

/**
 * Finds a type (class) binding that is a parent of given node.
 * 
 * @author Michal Borek
 */
public class BindingFinder {

    public ITypeBinding getParentTypeBinding(final ASTNode node) {
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
