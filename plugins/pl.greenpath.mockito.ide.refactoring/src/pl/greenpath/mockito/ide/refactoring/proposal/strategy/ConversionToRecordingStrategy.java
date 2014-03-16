package pl.greenpath.mockito.ide.refactoring.proposal.strategy;

public interface ConversionToRecordingStrategy {

    String getDescription();

    String getReturningMethodName();

    int getCursorPosition(String addedText);
}
