package pl.greenpath.mockito.ide.refactoring;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class PluginImagesTest {

    @Test
    public void shouldReuseSameInstance() {
        assertThat(PluginImages.getImageRegistry()).isNotNull();
        assertThat(PluginImages.getImageRegistry()).isSameAs(PluginImages.getImageRegistry());
    }

}
