package pl.greenpath.mockito.ide.refactoring.ast;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;

import pl.greenpath.mockito.ide.refactoring.quickfix.exception.NotSupportedRefactoring;

/**
 * This class is responsible for finding type of given selection (expression),
 * base on its context.
 * 
 * Context may be e.g. a method or constructor invocation.
 * 
 * For instance: Having <code>new ArrayList<String>(somePhrase)</code> this
 * finder for somePhrase will find Collection<? extends String>.
 * 
 * By now this method returns first result it finds.
 * 
 * @author Michal Borek
 */
public class ContextBaseTypeFinder {

    private final SimpleName selectedExpression;
    private final ASTNode parent;

    public ContextBaseTypeFinder(final SimpleName selectedExpression) {
        this.selectedExpression = selectedExpression;
        parent = selectedExpression.getParent();
    }

    /**
     * Finds best matching type for selectedExpression given in constructor
     * 
     * @return possible type of selectedExpression
     * @throws NotSupportedRefactoring
     *             when this finder does not support the context of expression
     *             usage.
     */
    public ITypeBinding find() throws NotSupportedRefactoring {
        final int index = getParentMembers().indexOf(selectedExpression);
        switch (parent.getNodeType()) {
        case ASTNode.CLASS_INSTANCE_CREATION:
            return getProposedType(index, (ClassInstanceCreation) parent);
        case ASTNode.METHOD_INVOCATION:
            return getProposedType(index, (MethodInvocation) parent);
        }
        throw new NotSupportedRefactoring("Type of invocation not supported by this fix processor: "
                + parent.getClass().getName());
    }

    @SuppressWarnings("rawtypes")
    private List getParentMembers() {
        return (List) selectedExpression.getParent().getStructuralProperty(selectedExpression.getLocationInParent());
    }

    private ITypeBinding getProposedType(final int index, final MethodInvocation parent) {
        return getTypeFromBinding(index, parent.resolveMethodBinding());
    }

    private ITypeBinding getProposedType(final int index, final ClassInstanceCreation parent) {
        return getTypeFromBinding(index, parent.resolveConstructorBinding());
    }

    private ITypeBinding getTypeFromBinding(final int index, final IMethodBinding binding) {
        return binding.getParameterTypes()[index];
    }
}
