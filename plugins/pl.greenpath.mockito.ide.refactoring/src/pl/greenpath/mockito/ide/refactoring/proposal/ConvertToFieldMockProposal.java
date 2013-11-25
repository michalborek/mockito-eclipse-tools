package pl.greenpath.mockito.ide.refactoring.proposal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.text.java.correction.ASTRewriteCorrectionProposal;
import org.eclipse.swt.graphics.Image;

import pl.greenpath.mockito.ide.refactoring.PluginImages;
import pl.greenpath.mockito.ide.refactoring.ast.AstResolver;
import pl.greenpath.mockito.ide.refactoring.ast.BindingFinder;
import pl.greenpath.mockito.ide.refactoring.builder.FieldDeclarationBuilder;
import pl.greenpath.mockito.ide.refactoring.builder.TypeSingleMemberAnnotationBuilder;

public class ConvertToFieldMockProposal extends ASTRewriteCorrectionProposal {

    private static final String MOCK = "org.mockito.Mock";
    private static final String MOCKITO_JUNIT_RUNNER = "org.mockito.runners.MockitoJUnitRunner";
    private static final String RUN_WITH = "org.junit.runner.RunWith";
    private final VariableDeclarationStatement _selectedStatement;
    private final CompilationUnit _astRoot;
    private final MethodDeclaration _methodBodyDeclaration;

    public ConvertToFieldMockProposal(final ICompilationUnit cu, final VariableDeclarationStatement selectedNode,
            final CompilationUnit astRoot) {
        super("Convert to field mock", cu, null, 0);
        _selectedStatement = selectedNode;
        _astRoot = astRoot;
        _methodBodyDeclaration = new AstResolver().findParentOfType(selectedNode, MethodDeclaration.class);
    }

    @Override
    protected ASTRewrite getRewrite() throws CoreException {
        final ASTRewrite rewrite = ASTRewrite.create(_selectedStatement.getAST());
        createImportRewrite(_astRoot);
        addMissingFieldDeclaration(rewrite);
        addRunWithAnnotation(rewrite);
        removeStatement(rewrite);
        return rewrite;
    }

    private void removeStatement(final ASTRewrite rewrite) throws CoreException {
        final ListRewrite list = rewrite.getListRewrite(_methodBodyDeclaration.getBody(), Block.STATEMENTS_PROPERTY);
        list.remove(new AstResolver().findParentOfType(_selectedStatement, Statement.class), null);
    }

    private void addRunWithAnnotation(final ASTRewrite rewrite) {
        new TypeSingleMemberAnnotationBuilder(new BindingFinder().getParentTypeBinding(_methodBodyDeclaration),
                _astRoot,
                rewrite, getImportRewrite())
                .setQualifiedName(RUN_WITH)
                .setValue(MOCKITO_JUNIT_RUNNER)
                .build();
    }

    private void addMissingFieldDeclaration(final ASTRewrite rewrite) {
        final VariableDeclarationFragment declaration =
                ((VariableDeclarationFragment) _selectedStatement.fragments().get(0));
        final FieldDeclarationBuilder builder = new FieldDeclarationBuilder(declaration.getName(), _astRoot, rewrite,
                getImportRewrite())
                .setType(_selectedStatement.getType().resolveBinding())
                .setModifiers(ModifierKeyword.PRIVATE_KEYWORD);

        final MethodInvocation mockMethodInvocation = (MethodInvocation) declaration.getInitializer();
        final boolean addedNormal = considerAnnotationWithExtraInterfaces(builder, mockMethodInvocation);
        if (!addedNormal) {
            builder.setMarkerAnnotation(MOCK);
        }
        builder.build();
    }

    @SuppressWarnings("unchecked")
    private boolean considerAnnotationWithExtraInterfaces(final FieldDeclarationBuilder builder,
            final MethodInvocation mockInvocation) {
        if (mockInvocation.arguments().size() != 2) {
            return false;
        }
        if (!(mockInvocation.arguments().get(1) instanceof MethodInvocation)) {
            return false;
        }
        final MethodInvocation mockMethodSecondArgument = (MethodInvocation) mockInvocation.arguments().get(1);
        if (!mockMethodSecondArgument.getName().getIdentifier().equals("extraInterfaces")) {
            return false;
        }
        
        builder.setAnnotationWithExtraInterfaces(MOCK, (TypeLiteral[]) mockMethodSecondArgument.arguments().toArray(new TypeLiteral[0]));
        return true;
    }

    @Override
    public Image getImage() {
        return PluginImages.get(ISharedImages.IMG_FIELD_PRIVATE);
    }

}
