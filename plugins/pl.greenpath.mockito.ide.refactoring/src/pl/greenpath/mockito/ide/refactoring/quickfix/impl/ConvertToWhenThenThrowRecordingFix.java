package pl.greenpath.mockito.ide.refactoring.quickfix.impl;

import pl.greenpath.mockito.ide.refactoring.proposal.strategy.WhenThenReturnRecordingStrategy;

public class ConvertToWhenThenThrowRecordingFix extends ConvertToRecordingFix {

    public ConvertToWhenThenThrowRecordingFix() {
        super(new WhenThenReturnRecordingStrategy());
    }

}
