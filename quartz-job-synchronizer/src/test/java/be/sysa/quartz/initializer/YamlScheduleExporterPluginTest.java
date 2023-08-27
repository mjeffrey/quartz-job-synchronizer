package be.sysa.quartz.initializer;

import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.Scheduler;
import org.quartz.simpl.SimpleClassLoadHelper;
import org.quartz.spi.ClassLoadHelper;

import java.io.File;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class YamlScheduleExporterPluginTest {
    private static final String PLUGIN_NAME = "test";
    private ClassLoadHelper classLoadHelper = new SimpleClassLoadHelper();

    YamlScheduleExporterPlugin plugin = new YamlScheduleExporterPlugin();

    @Mock
    private Scheduler scheduler;

    @SneakyThrows
    @Test
    @DisplayName("Export a schedule. Note the schedule is actually empty but the real export is tested elsewhere.")
    public void convertAnXmlFileAndVerifyItCanBeMapped() {
        when(scheduler.getSchedulerName()).thenReturn("JUNIT");
        File tempFile = File.createTempFile("jobs-from-xml", ".yaml");
        plugin.setExportFile(tempFile.getAbsolutePath());
        plugin.initialize(PLUGIN_NAME, scheduler, classLoadHelper);
        plugin.start();
        plugin.shutdown();
        assertThat(plugin.getName()).isEqualTo(PLUGIN_NAME);
        String file = FileUtils.readFileToString(tempFile, StandardCharsets.UTF_8);
        assertThat(file).contains("# Schedule exported from scheduler: 'JUNIT'");
    }
}
