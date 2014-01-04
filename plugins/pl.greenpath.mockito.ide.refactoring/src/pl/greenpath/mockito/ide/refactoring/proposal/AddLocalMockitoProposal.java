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
import org.eclipse.jdt.core.dom.Statement;
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

public class AddLocalMockitoProposal extends ASTRewriteCorrectionProposal {

    private static final String MOCKITO_PACKAGE = "org.mockito.Mockito";
    
    private final String _mockitoMethodName;
    private final SimpleName _selectedNode;
    private final CompilationUnit _astRoot;
    private final AST _ast;
    private final MethodDeclaration _methodBody;
    private ImportRewrite _importRewrite;
    private ContextSensitiveImportRewriteContext _importRewriteContext;
    private ExpressionStatement _initLocalMockExpression;
    private ASTRewrite _rewrite;

    public AddLocalMockitoProposal(final ICompilationUnit cu, final SimpleName selectedNode,
            final CompilationUnit astRoot, String mockitoMethodName) {
        super("Create local "+ mockitoMethodName, cu, null, 0);
        _selectedNode = selectedNode;
        _astRoot = astRoot;
        _ast = selectedNode.getAST();
        _methodBody = new AstResolver().findParentOfType(selectedNode, MethodDeclaration.class);
        _mockitoMethodName = mockitoMethodName;
    }

    @Override
    public ASTRewrite getRewrite() throws CoreException {
        _rewrite = ASTRewrite.create(_selectedNode.getAST());
        _importRewrite = createImportRewrite(_astRoot);
        _importRewriteContext = new ContextSensitiveImportRewriteContext(_methodBody, _importRewrite);
        try {
            performFix();
        } catch (final NotSupportedRefactoring e) {
            e.printStackTrace(); // TODO logging
        }
        return _rewrite;
    }

    @Override
    public int getRelevance() {
        if (_selectedNode.getIdentifier().toLowerCase().endsWith("mock")) {
            return 99;
        }
        return super.getRelevance();
    }

    @Override
    public Image getImage() {
        return PluginImages.get(ISharedImages.IMG_OBJS_LOCAL_VARIABLE);
    }

    public void performFix() throws NotSupportedRefactoring {
        setMockMethodInvocation(new ContextBaseTypeFinder(_selectedNode).find());
        final Statement currentStatement = new AstResolver().findParentOfType(_selectedNode, Statement.class);
        _rewrite.getListRewrite(_methodBody.getBody(), Block.STATEMENTS_PROPERTY).insertBefore(
                _initLocalMockExpression, currentStatement, null);
    }

    private VariableDeclarationExpression createVariable(final Type type) {
        final VariableDeclarationFragment fragment = _ast.newVariableDeclarationFragment();
        fragment.setName(_ast.newSimpleName(_selectedNode.getIdentifier()));
        final VariableDeclarationExpression variable = _ast.newVariableDeclarationExpression(fragment);
        variable.setType((Type) ASTNode.copySubtree(_ast, type));
        return variable;
    }

    private void setMockMethodInvocation(final ITypeBinding typeBinding) {
        importStaticMethod(MOCKITO_PACKAGE, _mockitoMethodName);
        _initLocalMockExpression = _ast.newExpressionStatement(createMockAssignment(importType(typeBinding)));
    }

    private Assignment createMockAssignment(final Type type) {
        final Assignment result = _ast.newAssignment();
        result.setLeftHandSide(createVariable(type));
        result.setRightHandSide(createMockMethodInvocation(type));
        return result;
    }

    @SuppressWarnings("unchecked")
    private MethodInvocation createMockMethodInvocation(final Type type) {
        final MethodInvocation result = _ast.newMethodInvocation();
        result.setName(_ast.newSimpleName(_mockitoMethodName));
        result.arguments().add(getTypeLiteral(type));
        return result;
    }

    private TypeLiteral getTypeLiteral(final Type type) {
        final TypeLiteral result = _ast.newTypeLiteral();
        result.setType((Type) ASTNode.copySubtree(_ast, getTypeForTypeLiteral(type)));
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
        return _importRewrite.addImport(typeBinding, _ast, _importRewriteContext);
    }

    private String importStaticMethod(final String qualifiedName, final String methodName) {
        return _importRewrite.addStaticImport(qualifiedName, methodName, false, _importRewriteContext);
    }

}
