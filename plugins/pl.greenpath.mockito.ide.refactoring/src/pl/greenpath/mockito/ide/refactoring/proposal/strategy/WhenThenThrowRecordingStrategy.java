package pl.greenpath.mockito.ide.refactoring.proposal.strategy;


public class WhenThenThrowRecordingStrategy implements ConversionToRecordingStrategy {

    private static final String METHOD_NAME = "thenThrow";

    private static final String METHOD_NAME_WITH_PARENTHESIS = METHOD_NAME + "(";

    @Override
    public String getDescription() {
        return "Convert to when(...).thenThrow(...)";
    }

    @Override
    public String getReturningMethodName() {
        return METHOD_NAME;
    }

    @Override
    public int getCursorPosition(final String addedText) {
        return addedText.indexOf(METHOD_NAME_WITH_PARENTHESIS) + METHOD_NAME_WITH_PARENTHESIS.length();
    }

}
