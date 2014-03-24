package pl.greenpath.mockito.ide.refactoring.proposal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;

import pl.greenpath.mockito.ide.refactoring.ast.AstResolver;
import pl.greenpath.mockito.ide.refactoring.proposal.strategy.ConversionToRecordingStrategy;

public class ToRecordingConverter extends ImportsModifier {

    private static final String MOCKITO_PACKAGE = "org.mockito.Mockito";
    private static final String MOCK_METHOD_NAME = "when";

    private final ConversionToRecordingStrategy strategy;
    private final ASTNode selectedNode;

    public ToRecordingConverter(final ImportRewrite importRewrite, final ASTNode selectedNode, final ConversionToRecordingStrategy strategy) {
        super(importRewrite, selectedNode.getAST());
        this.selectedNode = selectedNode;
        this.strategy = strategy;
    }

    public ASTRewrite performConversion() throws CoreException {
        final ASTNode newStatement = setMockMethodInvocation((Expression) selectedNode, null);
        final ASTRewrite rewrite = ASTRewrite.create(ast);
        rewrite.getListRewrite(getMethodBodyBlockContainingNode(), Block.STATEMENTS_PROPERTY).replace(
                getCurrentStatement(), newStatement, null);
        return rewrite;
    }

    private ExpressionStatement setMockMethodInvocation(final Expression expression, final Expression returnValue) {
        importStaticMethod(MOCKITO_PACKAGE, MOCK_METHOD_NAME);
        final Expression initLocalMockExpression = (Expression) ASTNode.copySubtree(ast, expression);

        final MethodInvocation result = createMockMethodInvocation(initLocalMockExpression);
        final MethodInvocation invocation = ast.newMethodInvocation();
        invocation.setExpression(result);
        invocation.setName(ast.newSimpleName(strategy.getReturningMethodName()));
        return ast.newExpressionStatement(invocation);

    }

    @SuppressWarnings("unchecked")
    private MethodInvocation createMockMethodInvocation(final Expression initLocalMockExpression) {
        final MethodInvocation result = ast.newMethodInvocation();
        result.setName(ast.newSimpleName(MOCK_METHOD_NAME));
        result.arguments().add(initLocalMockExpression);
        return result;
    }

    private Block getMethodBodyBlockContainingNode() {
        return new AstResolver().findParentOfType(selectedNode, MethodDeclaration.class).getBody();
    }

    private Statement getCurrentStatement() {
        return new AstResolver().findParentOfType(selectedNode, Statement.class);
    }
}
