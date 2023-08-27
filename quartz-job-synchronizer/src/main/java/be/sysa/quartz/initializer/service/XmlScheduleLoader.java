package be.sysa.quartz.initializer.service;

import be.sysa.quartz.initializer.xml.ScheduleDataXml;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

@Slf4j
@Getter
public class XmlScheduleLoader {
    private static final ObjectMapper objectMapper = new XmlMapper();

    @SneakyThrows
    public static ScheduleDataXml loadSchedule(InputStream inputStream){
        return objectMapper.readerFor(ScheduleDataXml.class).readValue(inputStream);
    }

}
