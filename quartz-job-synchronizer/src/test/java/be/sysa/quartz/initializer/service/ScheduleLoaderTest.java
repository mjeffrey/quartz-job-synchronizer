package be.sysa.quartz.initializer.service;

import be.sysa.quartz.initializer.api.ScheduleDefinitionApi;
import be.sysa.quartz.initializer.fixtures.TestFiles;
import be.sysa.quartz.initializer.fixtures.assertions.definition.JobDefinitionApiAssert;
import be.sysa.quartz.initializer.fixtures.assertions.definition.TriggerDefinitionApiAssert;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Map;

import static be.sysa.quartz.initializer.fixtures.ScheduleAssert.assertThat;


public class ScheduleLoaderTest {

    @SneakyThrows
    @Test
    @DisplayName("Schedule API is filed with expected data.")
    public void loadSchedule() {
        try (InputStream inputStream = TestFiles.yamlJobs()) {
            ScheduleDefinitionApi scheduleDefinitionApi = ScheduleLoader.loadSchedule(inputStream);
            assertThat(scheduleDefinitionApi).hasGroups("maximal", "minimal");
            JobDefinitionApiAssert jobAssert = assertThat(scheduleDefinitionApi)
                    .hasGroup("maximal")
                    .hasJob("MaximalJob");
            TriggerDefinitionApiAssert triggerAssert = jobAssert
                    .jobClass("be.sysa.quartz.initializer.fixtures.jobs.MyTestJob")
                    .hasTriggers("FileGeneration", "trigger2")
                    .recover(true)
                    .durable(false)
                    .noDescription()
                    .hasJobData(
                            Map.of("job-data-1", "2023-01-01",
                                    "job-data-2", 23)
                    )
                    .hasTrigger("FileGeneration");
            triggerAssert
                    .descriptionContains("")
                    .priority(10)
                    .cronExpressions(
                            "0 0 2 ? * MON,TUE,WED,THU,FRI *",
                            "0 1/5 6-20 ? * MON,TUE,WED,THU,FRI *"
                    )
                    .misfireExecution(true)
                    .timeZone("Europe/Brussels")
                    .hasJobData(
                            Map.of("datakey", "valueX",
                                    "datakey2", 0.123)
                    );

        }
    }

}

