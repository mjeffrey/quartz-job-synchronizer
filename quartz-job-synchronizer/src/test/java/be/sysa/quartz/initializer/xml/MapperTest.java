package be.sysa.quartz.initializer.xml;

import be.sysa.quartz.initializer.api.ScheduleDefinitionApi;
import be.sysa.quartz.initializer.fixtures.TestFiles;
import be.sysa.quartz.initializer.service.ScheduleLoader;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class MapperTest {

    @SneakyThrows
    @Test
    public void toModel() {
        try (InputStream inputStream = TestFiles.xmlToBeConverted()) {
            XmlMapper xmlMapper = new XmlMapper();
            ScheduleDataXml xml = xmlMapper.readValue(inputStream, ScheduleDataXml.class);
            ScheduleDefinitionApi xmlScheduleDefinitionApi = Mapper.toModel(xml);

            assertThat(xml.getSchedule().getJobs()).isNotEmpty();
            try (InputStream yamlInputStream = TestFiles.xmlExpectedConvertedToYaml()) {
                ScheduleDefinitionApi scheduleDefinitionApi = ScheduleLoader.loadSchedule(yamlInputStream);
                assertThat(xmlScheduleDefinitionApi).usingRecursiveComparison()
                        .ignoringFieldsMatchingRegexes("groups.*jobs.*dependencies")
                        .isEqualTo(scheduleDefinitionApi);
            }
        }

    }
}
