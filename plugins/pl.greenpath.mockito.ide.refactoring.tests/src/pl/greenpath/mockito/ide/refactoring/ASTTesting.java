package pl.greenpath.mockito.ide.refactoring;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class ASTTesting {
    
    public static CompilationUnit createAST(final ICompilationUnit compilationUnit) {
        final ASTParser parser = ASTParser.newParser(AST.JLS4);
        parser.setSource(compilationUnit);
        parser.setResolveBindings(true);
        return (CompilationUnit) parser.createAST(new NullProgressMonitor());
    }
}
