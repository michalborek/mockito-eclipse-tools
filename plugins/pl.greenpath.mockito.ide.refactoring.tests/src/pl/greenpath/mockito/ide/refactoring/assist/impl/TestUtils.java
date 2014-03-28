package pl.greenpath.mockito.ide.refactoring.assist.impl;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class TestUtils {
    public static final AST AST_INSTANCE = AST.newAST(AST.JLS4);

    public static VariableDeclarationStatement getVariableDeclarationStatement(
            final VariableDeclarationFragment variableDeclaration) {
        final VariableDeclarationStatement declaration = AST_INSTANCE
                .newVariableDeclarationStatement(variableDeclaration);
        createTypeStub(declaration);
        return declaration;
    }

    private static void createTypeStub(final VariableDeclarationStatement mockDeclaration) {
        final FieldDeclaration fieldDeclaration = AST_INSTANCE.newFieldDeclaration(createVariableDeclaration("Object",
                "conflicting"));
        final MethodDeclaration methodDeclaration = createMethodDeclaration(mockDeclaration);
        createTypeDeclaration(fieldDeclaration, methodDeclaration);
    }

    static VariableDeclarationFragment createVariableDeclaration(final String type, final String variableName) {
        final VariableDeclarationFragment fragment = AST_INSTANCE.newVariableDeclarationFragment();
        fragment.setName(AST_INSTANCE.newSimpleName(variableName));
        return fragment;
    }

    @SuppressWarnings("unchecked")
    private static TypeDeclaration createTypeDeclaration(final BodyDeclaration... bodyDeclarations) {
        final TypeDeclaration typeDeclaration = AST_INSTANCE.newTypeDeclaration();

        for (final BodyDeclaration bodyDeclaration : bodyDeclarations) {
            typeDeclaration.bodyDeclarations().add(bodyDeclaration);
        }
        return typeDeclaration;
    }

    @SuppressWarnings("unchecked")
    private static MethodDeclaration createMethodDeclaration(final VariableDeclarationStatement statement) {
        final MethodDeclaration newMethodDeclaration = AST_INSTANCE.newMethodDeclaration();
        newMethodDeclaration.setName(AST_INSTANCE.newSimpleName("method"));
        newMethodDeclaration.setBody(AST_INSTANCE.newBlock());
        newMethodDeclaration.getBody().statements().add(statement);
        return newMethodDeclaration;
    }

    @SuppressWarnings("unchecked")
    static MethodInvocation getMethodInvocation(final String methodName, final String className) {
        final MethodInvocation methodInvocation = AST_INSTANCE.newMethodInvocation();
        final SimpleType methodArgument = AST_INSTANCE.newSimpleType(AST_INSTANCE.newName(className));

        methodInvocation.setName(AST_INSTANCE.newSimpleName(methodName));
        final TypeLiteral typeLiteral = AST_INSTANCE.newTypeLiteral();
        typeLiteral.setType(methodArgument);
        methodInvocation.arguments().add(typeLiteral);
        return methodInvocation;
    }
}
