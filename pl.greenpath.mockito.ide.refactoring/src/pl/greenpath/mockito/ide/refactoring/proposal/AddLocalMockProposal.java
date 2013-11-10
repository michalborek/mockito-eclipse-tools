package pl.greenpath.mockito.ide.refactoring.proposal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;
import org.eclipse.swt.graphics.Image;

import pl.greenpath.mockito.ide.refactoring.PluginImages;
import pl.greenpath.mockito.ide.refactoring.ast.AstResolver;
import pl.greenpath.mockito.ide.refactoring.builder.LocalMockInitializationDeclarationBuilder;
import pl.greenpath.mockito.ide.refactoring.quickfix.exception.NotSupportedRefactoring;

public class AddLocalMockProposal extends ASTRewriteCorrectionProposal {

    private final SimpleName selectedNode;
    private final CompilationUnit astRoot;

    public AddLocalMockProposal(final ICompilationUnit cu, final SimpleName selectedNode,
            final CompilationUnit astRoot) {
        super("Create local mock", cu, null, 0);
        this.selectedNode = selectedNode;
        this.astRoot = astRoot;
    }

    @Override
    protected ASTRewrite getRewrite() throws CoreException {
        final ASTRewrite rewrite = ASTRewrite.create(selectedNode.getAST());
        createImportRewrite(astRoot);
        try {
            addMissingVariableDeclaration(rewrite);
        } catch (final NotSupportedRefactoring e) {
            e.printStackTrace(); // TODO logging
        }
        return rewrite;
    }

    private void addMissingVariableDeclaration(final ASTRewrite rewrite) throws NotSupportedRefactoring {
        new LocalMockInitializationDeclarationBuilder(selectedNode, new AstResolver().findParentMethodBodyDeclaration(selectedNode), astRoot, rewrite, getImportRewrite()).build();
    }

    @Override
    public int getRelevance() {
        if (selectedNode.getIdentifier().toLowerCase().endsWith("mock")) {
            return 99;
        }
        return super.getRelevance();
    }

    @Override
    public Image getImage() {
        return PluginImages.get(ISharedImages.IMG_OBJS_LOCAL_VARIABLE);
    }

}
