package pl.greenpath.mockito.ide.refactoring;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.ui.javaeditor.ASTProvider;

public class ASTTesting {
    
    public static CompilationUnit createAST(final ICompilationUnit compilationUnit) {
        final ASTParser parser = ASTParser.newParser(ASTProvider.SHARED_AST_LEVEL);
        parser.setSource(compilationUnit);
        parser.setResolveBindings(true);
        return (CompilationUnit) parser.createAST(new NullProgressMonitor());
    }
}
