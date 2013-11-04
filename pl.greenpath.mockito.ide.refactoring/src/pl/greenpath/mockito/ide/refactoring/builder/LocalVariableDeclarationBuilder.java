package pl.greenpath.mockito.ide.refactoring.builder;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
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

import pl.greenpath.mockito.ide.refactoring.ast.AstResolver;
import pl.greenpath.mockito.ide.refactoring.ast.BindingFinder;

public class LocalVariableDeclarationBuilder {

    private final ASTRewrite rewrite;
    private final AST ast;
    private final VariableDeclarationExpression variableDeclaration;
    private final ImportRewrite importRewrite;
    private final SimpleName selectedNode;
    private final AstResolver astResolver;
    private final BindingFinder bindingFinder;
    private final CompilationUnit parentClass;
    private final ImportRewriteContext importRewriteContext;
    private final BodyDeclaration methodBody;

    public LocalVariableDeclarationBuilder(final SimpleName selectedNode, final BodyDeclaration methodBody,
            final CompilationUnit parentClass, final ASTRewrite rewrite,
            final ImportRewrite importRewrite) {
        this.methodBody = methodBody;
        this.parentClass = parentClass;
        ast = selectedNode.getAST();
        this.selectedNode = selectedNode;
        this.rewrite = rewrite;
        this.importRewrite = importRewrite;
        astResolver = new AstResolver();
        bindingFinder = new BindingFinder();
        variableDeclaration = createVariableDeclaration();
        importRewriteContext = new ContextSensitiveImportRewriteContext(methodBody, importRewrite);
    }

    public void build() {
        addStaticImport("org.mockito.Mockito", "mock");
    }

    private VariableDeclarationExpression createVariableDeclaration() {
        final VariableDeclarationFragment fragment = ast.newVariableDeclarationFragment();
        fragment.setName(ast.newSimpleName(selectedNode.getIdentifier()));
        return ast.newVariableDeclarationExpression(fragment);
    }

    public LocalVariableDeclarationBuilder withMethodInvocation(final ITypeBinding type, final String fullyQualifiedName) {
        final Type typeName = addImport(type);
        variableDeclaration.setType(typeName);
        final Assignment newAssignment = ast.newAssignment();
        newAssignment.setLeftHandSide(variableDeclaration);
        final MethodInvocation newMethodInvocation = ast.newMethodInvocation();
        newMethodInvocation.setName(ast.newSimpleName("mock"));
        final TypeLiteral string = getTypeLiteral(typeName);
        newMethodInvocation.arguments().add(string);
        newAssignment.setRightHandSide(newMethodInvocation);

        rewrite.getListRewrite(((MethodDeclaration) methodBody).getBody(),
                Block.STATEMENTS_PROPERTY).insertFirst(ast.newExpressionStatement(newAssignment), null);
        return this;
    }

    private TypeLiteral getTypeLiteral(final Type type) {
        final TypeLiteral string = ast.newTypeLiteral();
        SimpleName newSimpleName = null;
        if(type.isParameterizedType()) {
            newSimpleName = ast.newSimpleName(((SimpleType) ((ParameterizedType) type).getType()).getName().getFullyQualifiedName());
        } else if (type.isSimpleType()) {
            newSimpleName = ast.newSimpleName(((SimpleType)type).getName().getFullyQualifiedName());
        }
        if(newSimpleName != null) {
            string.setType(ast.newSimpleType(newSimpleName));
        }
        return string;
    }

    private Type addImport(final ITypeBinding typeBinding) {
        return importRewrite.addImport(typeBinding, ast, importRewriteContext);
    }

    private String addStaticImport(final String fullyQualifiedName, final String methodName) {
        return importRewrite.addStaticImport(fullyQualifiedName, methodName, false, importRewriteContext);
    }
}
