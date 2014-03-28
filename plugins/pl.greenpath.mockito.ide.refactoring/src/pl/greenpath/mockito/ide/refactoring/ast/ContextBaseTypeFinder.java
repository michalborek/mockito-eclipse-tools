package pl.greenpath.mockito.ide.refactoring.ast;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import pl.greenpath.mockito.ide.refactoring.quickfix.exception.NotSupportedRefactoringException;

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

    private ASTNode selectedExpression;

    /**
     * Finds best matching type for selectedExpression given in constructor
     * 
     * @return possible type of selectedExpression
     * @throws NotSupportedRefactoringException
     *             when this finder does not support the context of expression
     *             usage.
     */
    public ITypeBinding find(final ASTNode selectedExpression) throws NotSupportedRefactoringException {
        this.selectedExpression = selectedExpression;
        final ASTNode parent = selectedExpression.getParent();
        switch (parent.getNodeType()) {
        case ASTNode.CLASS_INSTANCE_CREATION:
            return getProposedType((ClassInstanceCreation) parent);
        case ASTNode.METHOD_INVOCATION:
            return getProposedType((MethodInvocation) parent);
        case ASTNode.ARRAY_INITIALIZER:
            return getProposedType((ArrayInitializer) parent);
        case ASTNode.ASSIGNMENT:
            return getProposedType((Assignment) parent);
        case ASTNode.RETURN_STATEMENT:
            return getProposedType((ReturnStatement) parent);
        case ASTNode.VARIABLE_DECLARATION_FRAGMENT:
            return getProposedType((VariableDeclarationFragment) parent);
        }
        throw new NotSupportedRefactoringException("Type of invocation not supported by this fix processor: "
                + parent.getClass().getName());
    }

    private int getIndexOfParameterInMethodInvocation() {
        return getParentMembers().indexOf(selectedExpression);
    }

    @SuppressWarnings("rawtypes")
    private List getParentMembers() {
        return (List) selectedExpression.getParent().getStructuralProperty(selectedExpression.getLocationInParent());
    }

    private ITypeBinding getProposedType(final MethodInvocation parent) {
        return getTypeFromBinding(parent.resolveMethodBinding());
    }

    private ITypeBinding getProposedType(final ClassInstanceCreation parent) {
        return getTypeFromBinding(parent.resolveConstructorBinding());
    }

    private ITypeBinding getProposedType(final ArrayInitializer parent) {
        return parent.resolveTypeBinding().getComponentType();
    }

    private ITypeBinding getProposedType(final Assignment parent) {
        return parent.resolveTypeBinding();
    }

    private ITypeBinding getProposedType(final ReturnStatement parent) {
        return ((MethodDeclaration) parent.getParent().getParent()).getReturnType2().resolveBinding();
    }

    private ITypeBinding getProposedType(final VariableDeclarationFragment parent) {
        return parent.resolveBinding().getType();
    }

    private ITypeBinding getTypeFromBinding(final IMethodBinding binding) {
        return binding.getParameterTypes()[getIndexOfParameterInMethodInvocation()];
    }
}
