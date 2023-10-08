package be.sysa.quartz.initializer.service;

import be.sysa.quartz.initializer.api.ScheduleDefinitionApi;
import be.sysa.quartz.initializer.support.YamlObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * ScheduleLoader is a utility class for loading and writing ScheduleDefinitionApi objects.
 * It provides methods for loading a schedule from an input stream, writing a schedule to a string,
 * and writing a schedule to an output stream.
 */
@Slf4j
@Getter
public class ScheduleLoader {
    private static final ObjectMapper objectMapper = YamlObjectMapper.createObjectMapper();
    /**
     * Load the schedule definition from the given input stream.
     *
     * @param inputStream the input stream from which to load the schedule definition
     * @return the loaded ScheduleDefinitionApi object
     */
    @SneakyThrows
    public static ScheduleDefinitionApi loadSchedule(InputStream inputStream){
        return objectMapper.readerFor(ScheduleDefinitionApi.class).readValue(inputStream);
    }

    /**
     * Serialize the ScheduleDefinitionApi object to a string.
     *
     * @param scheduleDefinition the ScheduleDefinitionApi object to serialize
     * @return the serialized ScheduleDefinitionApi object as a string
     */
    @SneakyThrows
    public static String writeString(ScheduleDefinitionApi scheduleDefinition){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        writeStream(outputStream, scheduleDefinition);
        return outputStream.toString(StandardCharsets.UTF_8 );
    }

    /**
     * Serializes the ScheduleDefinitionApi object to the given output stream.
     *
     * @param outputStream        the output stream to write the serialized data to
     * @param scheduleDefinition  the ScheduleDefinitionApi object to serialize
     */
    @SneakyThrows
    public static void writeStream(OutputStream outputStream, ScheduleDefinitionApi scheduleDefinition){
        objectMapper.writerFor(ScheduleDefinitionApi.class).writeValue(outputStream, scheduleDefinition);
    }


}
