package pl.greenpath.mockito.ide.refactoring.proposal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;
import org.eclipse.swt.graphics.Image;

import pl.greenpath.mockito.ide.refactoring.PluginImages;
import pl.greenpath.mockito.ide.refactoring.ast.AstResolver;
import pl.greenpath.mockito.ide.refactoring.quickfix.exception.NotSupportedRefactoring;

public class ConvertToMockRecordProposal extends ASTRewriteCorrectionProposal {

    private static final String MOCKITO_PACKAGE = "org.mockito.Mockito";
    private static final String MOCK_METHOD_NAME = "when";

    private final ASTNode _selectedNode;
    private final CompilationUnit _astRoot;
    private final AST _ast;
    private final MethodDeclaration _methodBody;
    private ContextSensitiveImportRewriteContext _importRewriteContext;
    private Expression _initLocalMockExpression;
    private ASTRewrite _rewrite;
    private int _start;

    public ConvertToMockRecordProposal(final ICompilationUnit cu, final ASTNode selectedNode,
            final CompilationUnit astRoot) {
        super("Convert to when(...).thenReturn(...)", cu, null, 0);
        _selectedNode = selectedNode;
        _astRoot = astRoot;
        _ast = selectedNode.getAST();
        _methodBody = new AstResolver().findParentOfType(selectedNode, MethodDeclaration.class);
    }

    @Override
    public ASTRewrite getRewrite() throws CoreException {
        _rewrite = ASTRewrite.create(_selectedNode.getAST());
        createImportRewrite(_astRoot);
        _importRewriteContext = new ContextSensitiveImportRewriteContext(_methodBody, getImportRewrite());
        try {
            performFix();
        } catch (final NotSupportedRefactoring e) {
            e.printStackTrace(); // TODO logging
        }
        return _rewrite;
    }

    @Override
    public int getRelevance() {
        return 99;
    }

    @Override
    public Image getImage() {
        return PluginImages.get(ISharedImages.IMG_OBJS_LOCAL_VARIABLE);
    }

    public void performFix() throws NotSupportedRefactoring, CoreException {
        final Statement currentStatement = new AstResolver().findParentOfType(_selectedNode, Statement.class);

        final ASTNode newStatement = setMockMethodInvocation((Expression) _selectedNode, null);
        _rewrite.getListRewrite(_methodBody.getBody(), Block.STATEMENTS_PROPERTY).replace(
                currentStatement, newStatement, null);
    }

    private ExpressionStatement setMockMethodInvocation(final Expression expression, final Expression returnValue) {
        importStaticMethod(MOCKITO_PACKAGE, MOCK_METHOD_NAME);
        _initLocalMockExpression = (Expression) ASTNode.copySubtree(_ast, expression);

        final MethodInvocation result = createMockMethodInvocation(_initLocalMockExpression);
        final MethodInvocation invocation = _ast.newMethodInvocation();
        invocation.setExpression(result);
        invocation.setName(_ast.newSimpleName("thenReturn"));
        return _ast.newExpressionStatement(invocation);

    }

    @SuppressWarnings("unchecked")
    private MethodInvocation createMockMethodInvocation(final Expression initLocalMockExpression) {
        final MethodInvocation result = _ast.newMethodInvocation();
        result.setName(_ast.newSimpleName(MOCK_METHOD_NAME));
        result.arguments().add(initLocalMockExpression);
        return result;
    }

    private String importStaticMethod(final String qualifiedName, final String methodName) {
        return getImportRewrite().addStaticImport(qualifiedName, methodName, false, _importRewriteContext);
    }

}
