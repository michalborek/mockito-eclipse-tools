package pl.greenpath.mockito.ide.refactoring.ast;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;

/**
 * A helper class to find declarations based on given AST nodes
 * 
 * @author Michal Borek
 */
public class AstResolver {

    @SuppressWarnings("unchecked")
    public <T> T findParentOfType(final ASTNode node, final Class<T> type) {
        if (node == null) {
            return null;
        }
        if (type.isAssignableFrom(node.getClass())) {
            return (T) node;
        }
        return findParentOfType(node.getParent(), type);
    }

    public ChildListPropertyDescriptor getBodyDeclarationsProperty(final ASTNode node) {
        if (node instanceof AbstractTypeDeclaration) {
            return ((AbstractTypeDeclaration) node).getBodyDeclarationsProperty();
        }
        if (node instanceof AnonymousClassDeclaration) {
            return AnonymousClassDeclaration.BODY_DECLARATIONS_PROPERTY;
        }
        throw new IllegalArgumentException("Only AbstractTypeDeclaration and AnonymousClassDeclaration allowed");
    }
}
