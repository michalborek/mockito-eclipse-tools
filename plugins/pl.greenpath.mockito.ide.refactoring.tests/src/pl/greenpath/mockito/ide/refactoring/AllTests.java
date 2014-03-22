package pl.greenpath.mockito.ide.refactoring;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import pl.greenpath.mockito.ide.refactoring.ast.BindingFinderTest;
import pl.greenpath.mockito.ide.refactoring.proposal.AddLocalMockProposalTest;
import pl.greenpath.mockito.ide.refactoring.proposal.ContextBaseTypeFinderTest;
import pl.greenpath.mockito.ide.refactoring.proposal.ToRecordingConverterTest;

@RunWith(Suite.class)
@SuiteClasses({ AstResolverTest.class, BindingFinderTest.class, 
    AddLocalMockProposalTest.class, ContextBaseTypeFinderTest.class, ToRecordingConverterTest.class })
public class AllTests {
    

}
