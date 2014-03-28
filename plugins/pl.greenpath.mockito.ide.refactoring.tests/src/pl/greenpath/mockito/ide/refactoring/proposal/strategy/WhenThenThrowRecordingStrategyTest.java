package pl.greenpath.mockito.ide.refactoring.proposal.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class WhenThenThrowRecordingStrategyTest {

    @Test
    public void shouldReturnCursorPositionInsideThenReturn() {
        final WhenThenThrowRecordingStrategy testedClass = new WhenThenThrowRecordingStrategy();
        assertThat(testedClass.getCursorPosition("when(test.toString()).thenThrow())")).isEqualTo(32);
        assertThat(testedClass.getCursorPosition("when(test.thenThrow()).thenThrow())")).isEqualTo(33);
    }
}
