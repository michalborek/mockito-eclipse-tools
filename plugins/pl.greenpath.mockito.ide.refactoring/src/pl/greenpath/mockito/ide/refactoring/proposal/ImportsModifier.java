package pl.greenpath.mockito.ide.refactoring.proposal;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;

public class ImportsModifier {

    protected final ImportRewrite importRewrite;
    protected final AST ast;

    public ImportsModifier(final ImportRewrite importRewrite, final AST ast) {
        this.importRewrite = importRewrite;
        this.ast = ast;
    }

    protected Type importType(final ITypeBinding typeBinding) {
        return importRewrite.addImport(typeBinding, ast);
    }

    protected String importStaticMethod(final String qualifiedName, final String methodName) {
        return importRewrite.addStaticImport(qualifiedName, methodName, false);
    }

}