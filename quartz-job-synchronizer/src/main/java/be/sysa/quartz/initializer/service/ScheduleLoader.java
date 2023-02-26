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

@Slf4j
@Getter
public class ScheduleLoader {
    private static final ObjectMapper objectMapper = YamlObjectMapper.createObjectMapper();
    @SneakyThrows
    public static ScheduleDefinitionApi loadSchedule(InputStream inputStream){
        return objectMapper.readerFor(ScheduleDefinitionApi.class).readValue(inputStream);
    }

    @SneakyThrows
    public static String writeString(ScheduleDefinitionApi scheduleDefinition){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        writeStream(outputStream, scheduleDefinition);
        return outputStream.toString(StandardCharsets.UTF_8 );
    }

    @SneakyThrows
    public static void writeStream(OutputStream outputStream, ScheduleDefinitionApi scheduleDefinition){
        objectMapper.writerFor(ScheduleDefinitionApi.class).writeValue(outputStream, scheduleDefinition);
    }


}
