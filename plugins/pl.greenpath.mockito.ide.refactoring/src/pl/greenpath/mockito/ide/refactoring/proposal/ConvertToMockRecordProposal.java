package pl.greenpath.mockito.ide.refactoring.proposal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;

import pl.greenpath.mockito.ide.refactoring.PluginImages;
import pl.greenpath.mockito.ide.refactoring.proposal.strategy.ConversionToRecordingStrategy;

public class ConvertToMockRecordProposal extends ASTRewriteCorrectionProposal {

    private final ConversionToRecordingStrategy _strategy;
    private final ASTNode _selectedNode;
    private final CompilationUnit _astRoot;

    public ConvertToMockRecordProposal(final ICompilationUnit cu, final ASTNode selectedNode,
            final CompilationUnit astRoot, final ConversionToRecordingStrategy strategy) {
        super(strategy.getDescription(), cu, null, 0);
        _selectedNode = selectedNode;
        _astRoot = astRoot;
        _strategy = strategy;
    }

    @Override
    public ASTRewrite getRewrite() throws CoreException {
        return new ToRecordingConverter(_selectedNode.getAST(), createImportRewrite(_astRoot), _selectedNode, _strategy)
                .performConversion();
    }

    @Override
    public int getRelevance() {
        return 99;
    }

    @Override
    public Image getImage() {
        return PluginImages.get(ISharedImages.IMG_OBJS_LOCAL_VARIABLE);
    }

    @Override
    protected void performChange(final IEditorPart activeEditor, final IDocument document) throws CoreException {
        super.performChange(activeEditor, document);
        if (activeEditor != null) {
            putCursorIntoMethodInvocationBody(activeEditor);
        }
    }

    private void putCursorIntoMethodInvocationBody(final IEditorPart activeEditor) {
        final AbstractTextEditor editor = (AbstractTextEditor) activeEditor;
        final ISelectionProvider selectionProvider = editor.getSelectionProvider();
        if (!selectionProvider.getSelection().isEmpty()) {
            selectionProvider.setSelection(getAfterFixSelection((TextSelection) selectionProvider.getSelection()));
        }
    }

    private TextSelection getAfterFixSelection(final TextSelection initialSelection) {
        return new TextSelection(initialSelection.getOffset()
                + _strategy.getCursorPosition(initialSelection.getText()), 0);
    }

}
