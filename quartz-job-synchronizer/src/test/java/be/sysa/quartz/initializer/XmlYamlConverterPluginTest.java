package be.sysa.quartz.initializer;

import be.sysa.quartz.initializer.api.ScheduleDefinitionApi;
import be.sysa.quartz.initializer.model.Mapper;
import be.sysa.quartz.initializer.service.ScheduleLoader;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.simpl.SimpleClassLoadHelper;
import org.quartz.spi.ClassLoadHelper;

import java.io.File;
import java.io.FileInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

public class XmlYamlConverterPluginTest {
    private static final String PLUGIN_NAME = "test";
    private ClassLoadHelper classLoadHelper = new SimpleClassLoadHelper();

    XmlYamlConverterPlugin plugin = new XmlYamlConverterPlugin();

    @SneakyThrows
    @Test
    @DisplayName("Convert an XML file to Yaml and then verify it can be converted to a model.")
    public void convertAnXmlFileAndVerifyItCanBeMapped() {
        plugin.setImportFile("jobs-in-xml.xml");
        File tempFile = File.createTempFile("jobs-from-xml", ".yaml");
        plugin.setExportFile(tempFile.getAbsolutePath());
        plugin.initialize(PLUGIN_NAME, null, classLoadHelper);
        plugin.start();

        try(FileInputStream inputStream = new FileInputStream(tempFile)){
            ScheduleDefinitionApi schedule = ScheduleLoader.loadSchedule(inputStream);
            assertThatNoException().isThrownBy( ()->Mapper.toModel(schedule));
        }
        plugin.shutdown();
        assertThat(plugin.getName()).isEqualTo(PLUGIN_NAME);
    }
}
