package be.sysa.quartz.initializer;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.simpl.SimpleClassLoadHelper;
import org.quartz.spi.ClassLoadHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class YamlJobSynchronizerPluginTest {
    private static final String PLUGIN_NAME = "test";
    @Mock
    private Scheduler scheduler;

    private ClassLoadHelper classLoadHelper = new SimpleClassLoadHelper();

    YamlJobSynchronizerPlugin plugin = new YamlJobSynchronizerPlugin();

    @SneakyThrows
    @Test
    public void initialize() {
        plugin.setFileNames("jobs-from-xml.yaml");
        plugin.initialize(PLUGIN_NAME, scheduler, classLoadHelper);
        plugin.start();
        plugin.shutdown();
        assertThat(plugin.getName()).isEqualTo(PLUGIN_NAME);
        verify(scheduler, times(2)).addJob(any(JobDetail.class), anyBoolean(), anyBoolean());
        verify(scheduler, times(2)).scheduleJob(any(Trigger.class));
    }

}
