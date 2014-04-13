package pl.greenpath.mockito.ide.refactoring;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class PluginImagesTest {

    @Test
    public void shouldReuseSameInstance() {
        assertThat(PluginImages.getInstance().getImageRegistry()).isNotNull();
        assertThat(PluginImages.getInstance().getImageRegistry()).isSameAs(
                PluginImages.getInstance().getImageRegistry());
    }

}
