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
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;
import org.eclipse.swt.graphics.Image;

import pl.greenpath.mockito.ide.refactoring.PluginImages;
import pl.greenpath.mockito.ide.refactoring.ast.AstResolver;
import pl.greenpath.mockito.ide.refactoring.ast.ContextBaseTypeFinder;
import pl.greenpath.mockito.ide.refactoring.quickfix.exception.NotSupportedRefactoring;

public class AddLocalMockProposal extends ASTRewriteCorrectionProposal {

    private static final String MOCKITO_PACKAGE = "org.mockito.Mockito";
    private static final String MOCK_METHOD_NAME = "mock";

    private final SimpleName selectedNode;
    private final CompilationUnit astRoot;
    private final AST ast;
    private final MethodDeclaration methodBody;
    private ImportRewrite importRewrite;
    private ContextSensitiveImportRewriteContext importRewriteContext;
    private ExpressionStatement initLocalMockExpression;
    private ASTRewrite _rewrite;

    public AddLocalMockProposal(final ICompilationUnit cu, final SimpleName selectedNode,
            final CompilationUnit astRoot) {
        super("Create local mock", cu, null, 0);
        this.selectedNode = selectedNode;
        this.astRoot = astRoot;
        ast = selectedNode.getAST();
        
        methodBody = new AstResolver().findParentMethodBodyDeclaration(selectedNode);
    }

    @Override
    public ASTRewrite getRewrite() throws CoreException {
        _rewrite = ASTRewrite.create(selectedNode.getAST());
        importRewrite = createImportRewrite(astRoot);
        importRewriteContext = new ContextSensitiveImportRewriteContext(methodBody, importRewrite);
        try {
           build();
        } catch (final NotSupportedRefactoring e) {
            e.printStackTrace(); // TODO logging
        }
        return _rewrite;
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
    
    public void build() throws NotSupportedRefactoring {
        setMockMethodInvocation(new ContextBaseTypeFinder(selectedNode).find());
        _rewrite.getListRewrite(methodBody.getBody(), Block.STATEMENTS_PROPERTY).insertFirst(initLocalMockExpression,
                null);
    }

    private VariableDeclarationExpression createVariable(final Type type) {
        final VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
        fragment.setName(ast.newSimpleName(selectedNode.getIdentifier()));
        final VariableDeclarationExpression variable = ast.newVariableDeclarationExpression(fragment);
        variable.setType((Type) ASTNode.copySubtree(ast, type));
        return variable;
    }

    private void setMockMethodInvocation(final ITypeBinding typeBinding) {
        importStaticMethod(MOCKITO_PACKAGE, MOCK_METHOD_NAME);
        initLocalMockExpression = ast.newExpressionStatement(createMockAssignment(importType(typeBinding)));
    }

    private Assignment createMockAssignment(final Type type) {
        final Assignment result = ast.newAssignment();
        result.setLeftHandSide(createVariable(type));
        result.setRightHandSide(createMockMethodInvocation(type));
        return result;
    }

    @SuppressWarnings("unchecked")
    private MethodInvocation createMockMethodInvocation(final Type type) {
        final MethodInvocation result = ast.newMethodInvocation();
        result.setName(ast.newSimpleName(MOCK_METHOD_NAME));
        result.arguments().add(getTypeLiteral(type));
        return result;
    }

    private TypeLiteral getTypeLiteral(final Type type) {
        final TypeLiteral result = ast.newTypeLiteral();
        result.setType((Type) ASTNode.copySubtree(ast, getTypeForTypeLiteral(type)));
        return result;
    }

    private Type getTypeForTypeLiteral(final Type type) {
        if (type.isParameterizedType()) {
            return ((ParameterizedType) type).getType();
        } else {
            return type;
        }
    }

    private Type importType(final ITypeBinding typeBinding) {
        return importRewrite.addImport(typeBinding, ast, importRewriteContext);
    }

    private String importStaticMethod(final String qualifiedName, final String methodName) {
        return importRewrite.addStaticImport(qualifiedName, methodName, false, importRewriteContext);
    }

}
