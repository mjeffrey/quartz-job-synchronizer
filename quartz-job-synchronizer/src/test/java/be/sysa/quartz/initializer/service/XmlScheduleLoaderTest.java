package be.sysa.quartz.initializer.service;

import be.sysa.quartz.initializer.api.ScheduleDefinitionApi;
import be.sysa.quartz.initializer.fixtures.TestFiles;
import be.sysa.quartz.initializer.xml.ScheduleDataXml;
import be.sysa.quartz.initializer.xml.XmlMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.InputStream;

class XmlScheduleLoaderTest {

    @SneakyThrows
    @Test
    void loadSchedule() {

        try (InputStream inputStream = TestFiles.xmlToBeConverted()) {
            ScheduleDataXml scheduleDataXml = XmlScheduleLoader.loadSchedule(inputStream);
            ScheduleDefinitionApi scheduleDefinitionApi = XmlMapper.toModel(scheduleDataXml);
            System.out.println(ScheduleLoader.writeString(scheduleDefinitionApi));
        }

    }
}
