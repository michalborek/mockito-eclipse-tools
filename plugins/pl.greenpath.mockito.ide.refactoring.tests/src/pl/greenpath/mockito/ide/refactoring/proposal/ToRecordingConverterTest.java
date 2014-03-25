package pl.greenpath.mockito.ide.refactoring.proposal;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import pl.greenpath.mockito.ide.refactoring.proposal.strategy.ConversionToRecordingStrategy;
import pl.greenpath.mockito.ide.refactoring.proposal.strategy.WhenThenReturnRecordingStrategy;
import pl.greenpath.mockito.ide.refactoring.proposal.strategy.WhenThenThrowRecordingStrategy;

@RunWith(MockitoJUnitRunner.class)
public class ToRecordingConverterTest {

    private static final String STATIC_IMPORT = "org.mockito.Mockito.when";

    @Mock
    private ICompilationUnit cu;
    private final AST ast = AST.newAST(AST.JLS4);
    private ImportRewrite importRewrite;

    @Before
    public void before() throws JavaModelException {
        importRewrite = ImportRewrite.create(cu, false);
    }

    @Test
    public void shouldConvertParameterlessMethodInvocationWithThenReturnStrategy() throws CoreException {
        checkConversion(new WhenThenReturnRecordingStrategy(),
                "when(type.toString()).thenReturn();\n", getMethodInvocationExpression("type", "toString"));
    }

    @Test
    public void shouldConvertMethodWithParametersInvocation() throws CoreException {
        final ExpressionStatement expression = getMethodInvocationExpression("type", "equals", "x");
        checkConversion(new WhenThenReturnRecordingStrategy(), "when(type.equals(x)).thenReturn();\n", expression);
    }

    @Test
    public void shouldConvertWithThenThrowStrategy() throws CoreException {
        checkConversion(new WhenThenThrowRecordingStrategy(),
                "when(type.toString()).thenThrow();\n", getMethodInvocationExpression("type", "toString"));
    }

    private void checkConversion(final ConversionToRecordingStrategy strategy, final String resultStatement,
            final ExpressionStatement selectedExpression) throws CoreException {
        final MethodDeclaration method = getMethodDeclaration("testMethod", selectedExpression);

        final ToRecordingConverter testedClass = new ToRecordingConverter(importRewrite,
                selectedExpression.getExpression(),
                strategy);

        final ASTRewrite afterConversion = testedClass.performConversion();
        final ExpressionStatement resultExpression = (ExpressionStatement) afterConversion
                .getListRewrite(method.getBody(), Block.STATEMENTS_PROPERTY).getRewrittenList().get(0);
        assertThat(resultExpression.toString()).isEqualTo(resultStatement);
        assertThat(importRewrite.getAddedStaticImports()).contains(STATIC_IMPORT).describedAs(
                "Should add only %s, added: %s", STATIC_IMPORT,
                Arrays.toString(importRewrite.getAddedStaticImports()));
        assertThat(importRewrite.getAddedImports()).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private MethodDeclaration getMethodDeclaration(final String methodName, final ExpressionStatement selected) {
        final MethodDeclaration result = ast.newMethodDeclaration();
        result.setName(ast.newSimpleName(methodName));
        result.setBody(ast.newBlock());
        result.getBody().statements().add(selected);
        return result;
    }

    @SuppressWarnings("unchecked")
    private ExpressionStatement getMethodInvocationExpression(final String variable, final String invokedMethod,
            final String... arguments) {
        final MethodInvocation methodInvocation = ast.newMethodInvocation();
        methodInvocation.setName(ast.newSimpleName(invokedMethod));
        methodInvocation.setExpression(ast.newSimpleName(variable));
        for (final String argument : arguments) {
            methodInvocation.arguments().add(ast.newSimpleName(argument));
        }
        final ExpressionStatement selected = ast.newExpressionStatement(methodInvocation);
        return selected;
    }
}
