package be.sysa.quartz.initializer.service;

import be.sysa.quartz.initializer.api.ScheduleDefinitionApi;
import be.sysa.quartz.initializer.fixtures.TestFiles;
import be.sysa.quartz.initializer.fixtures.jobs.MyTestJob;
import be.sysa.quartz.initializer.model.Mapper;
import be.sysa.quartz.initializer.support.Errors;
import be.sysa.quartz.initializer.support.ScheduleDefinitionException;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import static org.apache.commons.lang3.StringUtils.replace;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


public class ModelMappingExceptionsTest {

    @Test
    @DisplayName("When the Job class is not found we fail fast.")
    public void jobClassNotFound() {
        ScheduleDefinitionApi scheduleDefinitionApi = getScheduleDefinitionApi(
                schedule -> replace(schedule, "be.sysa.quartz.", "notfound.")
        );
        assertThatExceptionOfType(ClassNotFoundException.class)
                .describedAs(scheduleDefinitionApi.toString())
                .isThrownBy( ()->Mapper.toModel(scheduleDefinitionApi));
    }

    @Test
    @DisplayName("When the Job class is not a Job we fail fast.")
    public void jobClassDoesNotImplementJob() {
        ScheduleDefinitionApi scheduleDefinitionApi = getScheduleDefinitionApi(
                schedule -> replace(schedule, MyTestJob.class.getName(), TestFiles.class.getName())
        );
        assertThatExceptionOfType(ScheduleDefinitionException.class)
                .describedAs(scheduleDefinitionApi.toString())
                .isThrownBy( ()->Mapper.toModel(scheduleDefinitionApi))
                .extracting(ScheduleDefinitionException::getErrorCode)
                .isEqualTo(Errors.JOB_CLASS_NOT_A_JOB);
    }

    @Test
    @DisplayName("Exception when a cron expression is invalid.")
    public void cronInvalid() {
        ScheduleDefinitionApi scheduleDefinitionApi = getScheduleDefinitionApi(
                schedule -> replace(schedule, "MON,TUE", "XXX,MAR")
        );
        assertThatExceptionOfType(ScheduleDefinitionException.class)
                .describedAs(scheduleDefinitionApi.toString())
                .isThrownBy( ()->Mapper.toModel(scheduleDefinitionApi))
                .extracting(ScheduleDefinitionException::getErrorCode)
                .isEqualTo(Errors.CRON_EXPRESSION_INVALID);
    }


    @Test
    @DisplayName("Exception when timezone is invalid (should be Europe/Brussels).")
    public void timeZoneInvalid() {
        ScheduleDefinitionApi scheduleDefinitionApi = getScheduleDefinitionApi(
                schedule -> replace(schedule, "Europe/Brussels", "europe/brussels")
        );
        assertThatExceptionOfType(ScheduleDefinitionException.class)
                .describedAs(scheduleDefinitionApi.toString())
                .isThrownBy( ()->Mapper.toModel(scheduleDefinitionApi))
                .extracting(ScheduleDefinitionException::getErrorCode)
                .isEqualTo(Errors.TIMEZONE_INVALID);
    }
    @Test
    @DisplayName("Groups must be unique in a schedule")
    public void groupsUnique() {
        Assertions.setMaxStackTraceElementsDisplayed(1000);
        ScheduleDefinitionApi scheduleDefinitionApi = getScheduleDefinitionApi(
                schedule -> replace(schedule, "group: \"minimal\"", "group: \"maximal\"")
        );
        assertThatExceptionOfType(ScheduleDefinitionException.class)
                .describedAs(scheduleDefinitionApi.toString())
                .isThrownBy( ()->Mapper.toModel(scheduleDefinitionApi))
                .extracting(ScheduleDefinitionException::getErrorCode)
                .isEqualTo(Errors.DUPLICATE_GROUP);
    }

    @SneakyThrows
    private static ScheduleDefinitionApi getScheduleDefinitionApi(Function<String, String> modification) {
        String schedule = IOUtils.toString(TestFiles.yamlJobs(), StandardCharsets.UTF_8);
        String replace = modification.apply(schedule);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(replace.getBytes());
        return ScheduleLoader.loadSchedule(inputStream);
    }

}

