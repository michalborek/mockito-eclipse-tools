package pl.greenpath.mockito.ide.refactoring.proposal;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;

import pl.greenpath.mockito.ide.refactoring.ast.AstResolver;
import pl.greenpath.mockito.ide.refactoring.ast.ContextBaseTypeFinder;
import pl.greenpath.mockito.ide.refactoring.proposal.strategy.ProposalStrategy;
import pl.greenpath.mockito.ide.refactoring.quickfix.exception.NotSupportedRefactoring;

public class ToLocalMockConverter extends ImportsModifier {

    private static final String MOCKITO_PACKAGE = "org.mockito.Mockito";

    private final ASTNode selectedNode;

    private final MethodDeclaration methodBody;

    private final ProposalStrategy proposalStrategy;

    private final ContextBaseTypeFinder contextBaseTypeFinder;

    public ToLocalMockConverter(final ImportRewrite importRewrite, final ASTNode selectedNode,
            final ProposalStrategy proposalStrategy) {
        this(importRewrite, selectedNode, proposalStrategy, new ContextBaseTypeFinder());
    }

    public ToLocalMockConverter(final ImportRewrite importRewrite, final ASTNode selectedNode,
            final ProposalStrategy proposalStrategy,
            final ContextBaseTypeFinder contextBaseTypeFinder) {
        super(importRewrite, selectedNode.getAST());
        this.selectedNode = selectedNode;
        this.proposalStrategy = proposalStrategy;
        this.contextBaseTypeFinder = contextBaseTypeFinder;
        this.methodBody = new AstResolver().findParentOfType(selectedNode, MethodDeclaration.class);
    }

    public ASTRewrite performFix() throws NotSupportedRefactoring {
        final ExpressionStatement initLocalMockExpression = createMockMethodInvocation(contextBaseTypeFinder
                .find(selectedNode));
        final ASTRewrite rewrite = ASTRewrite.create(selectedNode.getAST());
        final Statement currentStatement = new AstResolver().findParentOfType(selectedNode, Statement.class);
        rewrite.getListRewrite(methodBody.getBody(), Block.STATEMENTS_PROPERTY).insertBefore(
                initLocalMockExpression, currentStatement, null);
        return rewrite;
    }

    private ExpressionStatement createMockMethodInvocation(final ITypeBinding typeBinding) {
        importStaticMethod(MOCKITO_PACKAGE, proposalStrategy.getMockitoMethodName());
        return ast.newExpressionStatement(createMockAssignment(importType(typeBinding)));
    }

    private Assignment createMockAssignment(final Type type) {
        final Assignment result = ast.newAssignment();
        result.setLeftHandSide(createVariable(type));
        result.setRightHandSide(createMockMethodInvocation(type));
        return result;
    }

    private VariableDeclarationExpression createVariable(final Type type) {
        final VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
        fragment.setName(ast.newSimpleName(proposalStrategy.getVariableIdentifier()));
        final VariableDeclarationExpression variable = ast.newVariableDeclarationExpression(fragment);
        variable.setType((Type) ASTNode.copySubtree(ast, type));
        return variable;
    }

    @SuppressWarnings("unchecked")
    private MethodInvocation createMockMethodInvocation(final Type type) {
        final MethodInvocation result = ast.newMethodInvocation();
        result.setName(ast.newSimpleName(proposalStrategy.getMockitoMethodName()));
        result.arguments().add(proposalStrategy.getArgument(type));
        return result;
    }
}
