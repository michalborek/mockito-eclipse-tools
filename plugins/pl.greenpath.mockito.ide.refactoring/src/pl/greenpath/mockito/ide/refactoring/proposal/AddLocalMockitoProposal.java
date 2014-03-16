package pl.greenpath.mockito.ide.refactoring.proposal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;
import org.eclipse.swt.graphics.Image;

import pl.greenpath.mockito.ide.refactoring.PluginImages;
import pl.greenpath.mockito.ide.refactoring.ast.AstResolver;
import pl.greenpath.mockito.ide.refactoring.ast.ContextBaseTypeFinder;
import pl.greenpath.mockito.ide.refactoring.proposal.strategy.ProposalStrategy;
import pl.greenpath.mockito.ide.refactoring.quickfix.exception.NotSupportedRefactoring;

public class AddLocalMockitoProposal extends ASTRewriteCorrectionProposal {

    private static final String MOCKITO_PACKAGE = "org.mockito.Mockito";
    
    private final SimpleName selectedNode;
    private final CompilationUnit astRoot;
    private final AST ast;
    private final MethodDeclaration methodBody;
    private ImportRewrite importRewrite;
    private ExpressionStatement initLocalMockExpression;
    private ASTRewrite rewrite;
    private final ProposalStrategy proposalStrategy;

    public AddLocalMockitoProposal(final ICompilationUnit cu, final SimpleName selectedNode,
            final CompilationUnit astRoot, final  ProposalStrategy proposalStrategy) {
        super("Create local "+ proposalStrategy.getMockitoMethodName(), cu, null, 0);
        this.selectedNode = selectedNode;
        this.astRoot = astRoot;
        this.ast = selectedNode.getAST();
        this.methodBody = new AstResolver().findParentOfType(selectedNode, MethodDeclaration.class);
        this.proposalStrategy = proposalStrategy;
    }

    @Override
    public ASTRewrite getRewrite() throws CoreException {
        rewrite = ASTRewrite.create(selectedNode.getAST());
        importRewrite = createImportRewrite(astRoot);
        try {
            performFix();
        } catch (final NotSupportedRefactoring e) {
            e.printStackTrace(); // TODO logging
        }
        return rewrite;
    }

    @Override
    public int getRelevance() {
        if (selectedNode.getIdentifier().toLowerCase().endsWith("mock")) {
            return 99;
        }
        return super.getRelevance();
    }

    @Override
    public Image getImage() {
        return PluginImages.get(ISharedImages.IMG_OBJS_LOCAL_VARIABLE);
    }

    public void performFix() throws NotSupportedRefactoring {
        setMockMethodInvocation(new ContextBaseTypeFinder(selectedNode).find());
        final Statement currentStatement = new AstResolver().findParentOfType(selectedNode, Statement.class);
        rewrite.getListRewrite(methodBody.getBody(), Block.STATEMENTS_PROPERTY).insertBefore(
                initLocalMockExpression, currentStatement, null);
    }

    private void setMockMethodInvocation(final ITypeBinding typeBinding) {
        importStaticMethod(MOCKITO_PACKAGE, proposalStrategy.getMockitoMethodName());
        initLocalMockExpression = ast.newExpressionStatement(createMockAssignment(importType(typeBinding)));
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

    private Type importType(final ITypeBinding typeBinding) {
        return importRewrite.addImport(typeBinding, ast);
    }

    private String importStaticMethod(final String qualifiedName, final String methodName) {
        return importRewrite.addStaticImport(qualifiedName, methodName, false);
    }

}
