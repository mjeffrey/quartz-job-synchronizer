package be.sysa.quartz.initializer.model;

import be.sysa.quartz.initializer.api.GroupDefinitionApi;
import be.sysa.quartz.initializer.api.ScheduleDefinitionApi;
import be.sysa.quartz.initializer.fixtures.assertions.model.GroupDefinitionAssert;
import be.sysa.quartz.initializer.fixtures.assertions.model.JobDefinitionAssert;
import be.sysa.quartz.initializer.fixtures.assertions.model.TriggerDefinitionAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static be.sysa.quartz.initializer.fixtures.ScheduleAssert.assertThat;
import static be.sysa.quartz.initializer.fixtures.ScheduleFixture.maximalJob;
import static be.sysa.quartz.initializer.fixtures.ScheduleFixture.mimimalJob;

public class XmlMapperTest {

    @Test
    @DisplayName("Use minimal fixture to map to model and verify the contents (with defaults), One trigger with the same name created")
    public void toModelMinimal() {

        GroupDefinitionApi minimal = GroupDefinitionApi.builder().name("minimal").job(mimimalJob()).build();
        ScheduleDefinitionApi scheduleDefinitionApi = ScheduleDefinitionApi.builder()
                .group(minimal)
                .build();
        ScheduleDefinition scheduleDefinition = Mapper.toModel(scheduleDefinitionApi);
        GroupDefinitionAssert groupAssert = assertThat(scheduleDefinition).hasGroups("minimal").hasGroup("minimal");
        JobDefinitionAssert jobAssert = groupAssert.hasJobs("MinimalJob")
                .hasJob("MinimalJob");
        TriggerDefinitionAssert triggerAssert = jobAssert
                .hasTriggers("FileGeneration")
                .jobClass("be.sysa.quartz.initializer.fixtures.jobs.MyTestJob")
                .recover(false)
                .durable(true)
                .hasJobData(Collections.emptyMap())
                .hasTrigger("FileGeneration");

        triggerAssert.noDescription()
                .triggerGroup("minimal")
                .misfireExecution(false)
                .timeZone("UTC")
                .priority(5)
                .cronExpression("0 0 2 ? * MON,TUE,WED,THU,FRI *")
                .hasJobData(Collections.emptyMap());
    }

    @Test
    @DisplayName("Use maximal fixture to map to model and verify the contents. A trigger per cronExpression is created, with names: Trigger.[sequence]")
    public void toModelMaximal() {

        GroupDefinitionApi maximal = GroupDefinitionApi.builder().name("maximal").job(maximalJob()).build();
        ScheduleDefinitionApi scheduleDefinitionApi = ScheduleDefinitionApi.builder()
                .group(maximal)
                .build();
        ScheduleDefinition scheduleDefinition = Mapper.toModel(scheduleDefinitionApi);
        GroupDefinitionAssert groupAssert = assertThat(scheduleDefinition).hasGroups("maximal").hasGroup("maximal");
        JobDefinitionAssert jobAssert = groupAssert.hasJobs("MaximalJob")
                .hasJob("MaximalJob");
        jobAssert
                .hasTriggers("FileGeneration.1", "FileGeneration.2")
                .jobClass("be.sysa.quartz.initializer.fixtures.jobs.MyTestJob")
                .recover(true)
                .durable(true)
                .hasJobData(Map.of(
                        "Key1", "value1",
                        "Key2", 2
                ));

        jobAssert.hasTrigger("FileGeneration.1")
                .descriptionContains("every 5 minutes starting at minute 02, every hour between 06:00 and 20:00, every Weekday")
                .triggerGroup("triggerGroup")
                .misfireExecution(true)
                .timeZone("Europe/Brussels")
                .priority(10)
                .cronExpression("0 1/5 6-20 ? * MON,TUE,WED,THU,FRI *")
                .hasJobData(Map.of(
                        "datakey1", "valueX",
                        "datakey2", 0.123
                ));

        jobAssert.hasTrigger("FileGeneration.2")
                .descriptionContains("every 5 minutes starting at minute 02, every hour between 06:00 and 20:00, every Weekday")
                .triggerGroup("triggerGroup")
                .misfireExecution(true)
                .timeZone("Europe/Brussels")
                .priority(10)
                .cronExpression("0 0 2 ? * MON,TUE,WED,THU,FRI *")
                .hasJobData(Map.of(
                        "datakey1", "valueX",
                        "datakey2", 0.123
                ));

    }

}
