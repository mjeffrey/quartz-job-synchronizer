package be.sysa.quartz.initializer.service;

import be.sysa.quartz.initializer.xml.ScheduleDataXml;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * Class XmlScheduleLoader is responsible for loading schedule data from an XML file.
 *
 * This class provides a method to load the schedule data from an input stream.
 *
 * Usage:
 * 1. Create an instance of XmlScheduleLoader.
 * 2. Call the static method loadSchedule() and pass an input stream containing the XML data.
 * 3. The method will parse the XML data and return a ScheduleDataXml object.
 *
 * Example:
 * InputStream inputStream = new FileInputStream("schedule.xml");
 * ScheduleDataXml scheduleData = XmlScheduleLoader.loadSchedule(inputStream);
 *
 * @see ScheduleDataXml
 */
@Slf4j
@Getter
public class XmlScheduleLoader {
    private static final ObjectMapper objectMapper = new XmlMapper();

    /**
     * Loads and parses the ScheduleDataXml from the given input stream.
     *
     * @param inputStream the input stream containing the ScheduleDataXml
     * @return the ScheduleDataXml parsed from the input stream
     */
    @SneakyThrows
    public static ScheduleDataXml loadSchedule(InputStream inputStream){
        return objectMapper.readerFor(ScheduleDataXml.class).readValue(inputStream);
    }

}
