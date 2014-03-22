package pl.greenpath.mockito.ide.refactoring.proposal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import pl.greenpath.mockito.ide.refactoring.proposal.strategy.ConversionToRecordingStrategy;
import pl.greenpath.mockito.ide.refactoring.proposal.strategy.WhenThenReturnRecordingStrategy;

@RunWith(MockitoJUnitRunner.class)
public class ToRecordingConverterTest {

    @Mock
    private ICompilationUnit cu;
    private final AST ast = AST.newAST(AST.JLS4);

    @Test
    public void test() throws CoreException {
        final ConversionToRecordingStrategy strategy = new WhenThenReturnRecordingStrategy();
        final ExpressionStatement selected = getMethodInvocationStatement("type", "toString");
        System.err.println(selected);
        final ImportRewrite importRewrite = ImportRewrite.create(cu, false);
        final ToRecordingConverter testedClass = new ToRecordingConverter(ast, importRewrite, selected, strategy);
        testedClass.performConversion();
    }

    private ExpressionStatement getMethodInvocationStatement(final String variable, final String invokedMethod) {
        final MethodInvocation methodInvocation = ast.newMethodInvocation();
        methodInvocation.setName(ast.newSimpleName(invokedMethod));
        methodInvocation.setExpression(ast.newSimpleName(variable));
        final ExpressionStatement selected = ast.newExpressionStatement(methodInvocation);
        return selected;
    }

    private char[] getContents() {
        return ("class Test {"
                + "public void t() {"
                + "}"
                + "}").toCharArray();
    }

}
