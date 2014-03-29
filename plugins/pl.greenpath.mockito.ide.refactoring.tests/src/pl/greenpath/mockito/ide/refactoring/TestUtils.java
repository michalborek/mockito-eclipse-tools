package pl.greenpath.mockito.ide.refactoring;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.ExpressionStatement;
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

    public static VariableDeclarationStatement createVariableDeclarationStatement(
            final VariableDeclarationFragment declaration) {
        final VariableDeclarationStatement statement = AST_INSTANCE.newVariableDeclarationStatement(declaration);
        statement.setType(AST_INSTANCE.newSimpleType(AST_INSTANCE.newSimpleName("Object")));
        createTypeStub(statement);
        return statement;
    }

    /**
     * Creates a stub of a type (java class), which is needed by some tests that
     * search for fields in a class in which the conversion will be performed
     * 
     * @param mockDeclaration
     */
    private static void createTypeStub(final VariableDeclarationStatement mockDeclaration) {
        final FieldDeclaration fieldDeclaration = AST_INSTANCE.newFieldDeclaration(createVariableDeclaration("Object",
                "conflicting"));
        final MethodDeclaration methodDeclaration = createMethodDeclaration(mockDeclaration);
        createTypeDeclaration(fieldDeclaration, methodDeclaration);
    }

    /**
     * Creates variable declaration fragment with given type and variable name
     * 
     * @param type
     * @param variableName
     * @return Type variableName; 
     * TODO type is not used, remove it and use it in
     *         variable declaration statement
     */
    public static VariableDeclarationFragment createVariableDeclaration(final String type, final String variableName) {
        final VariableDeclarationFragment fragment = AST_INSTANCE.newVariableDeclarationFragment();
        fragment.setName(AST_INSTANCE.newSimpleName(variableName));
        return fragment;
    }

    /**
     * Creates
     * 
     * @param bodyDeclarations
     * @return
     */
    @SuppressWarnings("unchecked")
    private static TypeDeclaration createTypeDeclaration(final BodyDeclaration... bodyDeclarations) {
        final TypeDeclaration typeDeclaration = AST_INSTANCE.newTypeDeclaration();

        for (final BodyDeclaration bodyDeclaration : bodyDeclarations) {
            typeDeclaration.bodyDeclarations().add(bodyDeclaration);
        }
        return typeDeclaration;
    }

    /**
     * Creates method declaration with given statement
     * 
     * @param statement
     * @return void method() { statement; }
     */
    @SuppressWarnings("unchecked")
    private static MethodDeclaration createMethodDeclaration(final VariableDeclarationStatement statement) {
        final MethodDeclaration newMethodDeclaration = AST_INSTANCE.newMethodDeclaration();
        newMethodDeclaration.setName(AST_INSTANCE.newSimpleName("method"));
        newMethodDeclaration.setBody(AST_INSTANCE.newBlock());
        newMethodDeclaration.getBody().statements().add(statement);
        return newMethodDeclaration;
    }

    /**
     * Creates method invocation with class of given className as a parameter
     * 
     * @param methodName
     * @param className
     * @return methodName(className.class)
     */
    @SuppressWarnings("unchecked")
    public static MethodInvocation createMethodInvocation(final String methodName, final String className) {
        final MethodInvocation methodInvocation = AST_INSTANCE.newMethodInvocation();
        final SimpleType methodArgument = AST_INSTANCE.newSimpleType(AST_INSTANCE.newName(className));

        methodInvocation.setName(AST_INSTANCE.newSimpleName(methodName));
        final TypeLiteral typeLiteral = AST_INSTANCE.newTypeLiteral();
        typeLiteral.setType(methodArgument);
        methodInvocation.arguments().add(typeLiteral);
        return methodInvocation;
    }

    /**
     * Creates method declaration of name "method" with statement inside
     * 
     * @param methodName
     * @param statement
     * @return void method { selected }
     */
    @SuppressWarnings("unchecked")
    public static MethodDeclaration createMethodDeclaration(final String methodName, final ExpressionStatement statement) {
        final MethodDeclaration result = AST_INSTANCE.newMethodDeclaration();
        result.setName(AST_INSTANCE.newSimpleName(methodName));
        result.setBody(AST_INSTANCE.newBlock());
        result.getBody().statements().add(statement);
        return result;
    }

    /**
     * Creates method invocation on specified variable.
     * 
     * @param variable
     * @param invokedMethod
     * @param arguments
     * @return variable.invokedMethod(arguments);
     */
    @SuppressWarnings("unchecked")
    public static ExpressionStatement createMethodInvocationExpression(final String variable,
            final String invokedMethod,
            final String... arguments) {
        final MethodInvocation methodInvocation = AST_INSTANCE.newMethodInvocation();
        methodInvocation.setName(AST_INSTANCE.newSimpleName(invokedMethod));
        methodInvocation.setExpression(AST_INSTANCE.newSimpleName(variable));
        for (final String argument : arguments) {
            methodInvocation.arguments().add(AST_INSTANCE.newSimpleName(argument));
        }
        final ExpressionStatement selected = AST_INSTANCE.newExpressionStatement(methodInvocation);
        return selected;
    }

}
