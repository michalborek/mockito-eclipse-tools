package pl.greenpath.mockito.ide.refactoring.proposal.strategy;


public class WhenThenReturnRecordingStrategy implements ConversionToRecordingStrategy {

    private static final String THEN_RETURN = "thenReturn";

    private static final String METHOD_PREFIX = THEN_RETURN + "(";
    
    @Override
    public String getDescription() {
        return "Convert to when(...).thenReturn(...)";
    }

    @Override
    public String getReturningMethodName() {
        return THEN_RETURN;
    }

    @Override
    public int getCursorPosition(final String addedText) {
        return addedText.lastIndexOf(METHOD_PREFIX) + METHOD_PREFIX.length();
    }

}
