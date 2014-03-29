package pl.greenpath.mockito.ide.refactoring.quickfix.impl;

import pl.greenpath.mockito.ide.refactoring.proposal.strategy.WhenThenReturnRecordingStrategy;

public class ConvertToWhenThenReturnRecordingFix extends ConvertToRecordingFix {

    public ConvertToWhenThenReturnRecordingFix() {
        super(new WhenThenReturnRecordingStrategy());
    }

}
