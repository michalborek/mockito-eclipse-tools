package pl.greenpath.mockito.ide.refactoring.builder;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite.ImportRewriteContext;
import org.eclipse.jdt.internal.corext.codemanipulation.ContextSensitiveImportRewriteContext;

public class LocalMockInitializationDeclarationBuilder {

    private static final String MOCKITO_PACKAGE = "org.mockito.Mockito";
    private static final String MOCK_METHOD_NAME = "mock";
    private final ASTRewrite rewrite;
    private final AST ast;
    private final ImportRewrite importRewrite;
    private final SimpleName selectedNode;
    private final ImportRewriteContext importRewriteContext;
    private final MethodDeclaration methodBody;
    private ExpressionStatement initLocalMockExpression;

    public LocalMockInitializationDeclarationBuilder(final SimpleName selectedNode,
            final MethodDeclaration methodBody,
            final CompilationUnit parentClass, final ASTRewrite rewrite,
            final ImportRewrite importRewrite) {
        this.methodBody = methodBody;
        this.selectedNode = selectedNode;
        this.rewrite = rewrite;
        this.importRewrite = importRewrite;
        ast = selectedNode.getAST();
        importRewriteContext = new ContextSensitiveImportRewriteContext(methodBody, importRewrite);
    }

    public void build() {
        rewrite.getListRewrite(methodBody.getBody(), Block.STATEMENTS_PROPERTY).insertFirst(initLocalMockExpression,
                null);
    }

    private VariableDeclarationExpression createVariable(final Type typeName) {
        final VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
        fragment.setName(ast.newSimpleName(selectedNode.getIdentifier()));
        final VariableDeclarationExpression variable = ast.newVariableDeclarationExpression(fragment);
        variable.setType(typeName);
        return variable;
    }

    public LocalMockInitializationDeclarationBuilder withMethodInvocation(final ITypeBinding typeBinding) {
        importStaticMethod(MOCKITO_PACKAGE, MOCK_METHOD_NAME);
        initLocalMockExpression = ast.newExpressionStatement(createMockAssignment(importType(typeBinding)));
        return this;
    }

    private Assignment createMockAssignment(final Type typeName) {
        final Assignment result = ast.newAssignment();
        result.setLeftHandSide(createVariable(typeName));
        result.setRightHandSide(createMockMethodInvocation(typeName));
        return result;
    }

    @SuppressWarnings("unchecked")
    private MethodInvocation createMockMethodInvocation(final Type typeName) {
        final MethodInvocation result = ast.newMethodInvocation();
        result.setName(ast.newSimpleName(MOCK_METHOD_NAME));
        result.arguments().add(getTypeLiteral(typeName));
        return result;
    }

    private TypeLiteral getTypeLiteral(final Type type) {
        final TypeLiteral result = ast.newTypeLiteral();
        result.setType(ast.newSimpleType(ast.newSimpleName(getTypeName(type))));
        return result;
    }

    private String getTypeName(final Type type) {
        final SimpleType simpleType;
        if (type.isParameterizedType()) {
            simpleType = (SimpleType) ((ParameterizedType) type).getType();
        } else if (type.isSimpleType()) {
            simpleType = (SimpleType) type;
        } else {
            throw new IllegalArgumentException("Type: " + type.getClass() + " is not supported");
        }
        return simpleType.getName().getFullyQualifiedName();
    }

    private Type importType(final ITypeBinding typeBinding) {
        return importRewrite.addImport(typeBinding, ast, importRewriteContext);
    }

    private String importStaticMethod(final String qualifiedName, final String methodName) {
        return importRewrite.addStaticImport(qualifiedName, methodName, false, importRewriteContext);
    }
}
