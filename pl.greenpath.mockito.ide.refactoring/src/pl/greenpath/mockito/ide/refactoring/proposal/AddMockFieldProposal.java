package pl.greenpath.mockito.ide.refactoring.proposal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;
import org.eclipse.swt.graphics.Image;

import pl.greenpath.mockito.ide.refactoring.PluginImages;
import pl.greenpath.mockito.ide.refactoring.ast.BindingFinder;
import pl.greenpath.mockito.ide.refactoring.ast.ContextBaseTypeFinder;
import pl.greenpath.mockito.ide.refactoring.builder.FieldDeclarationBuilder;
import pl.greenpath.mockito.ide.refactoring.builder.TypeSingleMemberAnnotationBuilder;
import pl.greenpath.mockito.ide.refactoring.quickfix.exception.NotSupportedRefactoring;

public class AddMockFieldProposal extends ASTRewriteCorrectionProposal {

    private static final String MOCK = "org.mockito.Mock";
    private static final String MOCKITO_JUNIT_RUNNER = "org.mockito.runners.MockitoJUnitRunner";
    private static final String RUN_WITH = "org.junit.runner.RunWith";
    private final SimpleName selectedNode;
    private final CompilationUnit astRoot;

    public AddMockFieldProposal(final ICompilationUnit cu, final SimpleName selectedNode,
            final CompilationUnit astRoot) {
        super("Create field mock", cu, null, 0);
        this.selectedNode = selectedNode;
        this.astRoot = astRoot;
    }

    @Override
    protected ASTRewrite getRewrite() throws CoreException {
        final ASTRewrite rewrite = ASTRewrite.create(selectedNode.getAST());
        createImportRewrite(astRoot);
        try {
            addMissingFieldDeclaration(rewrite);
        } catch (final NotSupportedRefactoring e) {
            // TODO logging
            e.printStackTrace();
        }
        addRunWithAnnotation(rewrite);
        return rewrite;
    }

    private void addRunWithAnnotation(final ASTRewrite rewrite) {
        new TypeSingleMemberAnnotationBuilder(new BindingFinder().getParentTypeBinding(selectedNode), astRoot,
                rewrite, getImportRewrite())
                .setQualifiedName(RUN_WITH)
                .setValue(MOCKITO_JUNIT_RUNNER)
                .build();
    }

    private void addMissingFieldDeclaration(final ASTRewrite rewrite) throws NotSupportedRefactoring {
        new FieldDeclarationBuilder(selectedNode, astRoot, rewrite, getImportRewrite())
                .setType(new ContextBaseTypeFinder(selectedNode).find())
                .setModifiers(ModifierKeyword.PRIVATE_KEYWORD)
                .setMarkerAnnotation(MOCK)
                .build();
    }

    @Override
    public int getRelevance() {
        if (selectedNode.getIdentifier().toLowerCase().endsWith("mock")) {
            return 98;
        }
        return super.getRelevance();
    }

    @Override
    public Image getImage() {
        return PluginImages.get(ISharedImages.IMG_FIELD_PRIVATE);
    }

}
