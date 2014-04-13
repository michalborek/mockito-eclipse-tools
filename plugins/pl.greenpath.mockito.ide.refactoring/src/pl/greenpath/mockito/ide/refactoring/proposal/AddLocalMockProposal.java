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
import pl.greenpath.mockito.ide.refactoring.proposal.strategy.ProposalStrategy;

public class AddLocalMockProposal extends ASTRewriteCorrectionProposal {

    private final SimpleName selectedNode;
    private final CompilationUnit astRoot;
    private final ProposalStrategy proposalStrategy;

    public AddLocalMockProposal(final ICompilationUnit cu, final SimpleName selectedNode,
            final CompilationUnit astRoot, final ProposalStrategy proposalStrategy) {
        super("Create local " + proposalStrategy.getMockitoMethodName(), cu, null, 0);
        this.selectedNode = selectedNode;
        this.astRoot = astRoot;
        this.proposalStrategy = proposalStrategy;
    }

    @Override
    public ASTRewrite getRewrite() throws CoreException {
        createImportRewrite(astRoot);
        return new ToLocalMockConverter(getImportRewrite(), selectedNode, proposalStrategy).performFix();
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
        return PluginImages.getInstance().get(ISharedImages.IMG_OBJS_LOCAL_VARIABLE);
    }

    @Override
    public String toString() {
        return "AddLocalMockProposal [selectedNode=" + selectedNode + ", proposalStrategy=" + proposalStrategy + "]";
    }

}
