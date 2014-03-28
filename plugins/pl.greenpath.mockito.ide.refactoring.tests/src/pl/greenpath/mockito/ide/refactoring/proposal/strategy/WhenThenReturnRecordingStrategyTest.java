package pl.greenpath.mockito.ide.refactoring.proposal.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class WhenThenReturnRecordingStrategyTest {

    @Test
    public void shouldReturnCursorPositionInsideThenReturn() {
        final WhenThenReturnRecordingStrategy testedClass = new WhenThenReturnRecordingStrategy();
        assertThat(testedClass.getCursorPosition("when(test.toString()).thenReturn())")).isEqualTo(33);
        assertThat(testedClass.getCursorPosition("when(test.thenReturn()).thenReturn())")).isEqualTo(35);
    }
}
