package be.sysa.quartz.initializer;

import be.sysa.quartz.initializer.fixtures.QuartzYamlPluginFixture;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.Scheduler;
import org.quartz.spi.ClassLoadHelper;

import java.io.ByteArrayInputStream;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class YamlJobSynchronizerPluginTest {
    @Mock
    private Scheduler scheduler;
    @Mock
    private ClassLoadHelper classLoadHelper;

    YamlJobSynchronizerPlugin plugin = new YamlJobSynchronizerPlugin();

    @Test
    @Disabled
    public void initialize() {
        plugin.setFileNames("file1");
        plugin.initialize("test", scheduler, classLoadHelper);
        when(classLoadHelper.getResourceAsStream("file1")).thenReturn(new ByteArrayInputStream("xx".getBytes()));
        plugin.start();
    }

    @Test
    @Disabled
    public void name() {
        QuartzYamlPluginFixture fixture = QuartzYamlPluginFixture.init("jobs.yaml");
        YamlJobSynchronizerPlugin start = fixture.start();
    }

    //    @Test
//    public void start() {
//    }
//
//    @Test
//    public void shutdown() {
//    }
}
