package be.sysa.quartz.initializer.service;

import be.sysa.quartz.initializer.api.ScheduleDefinitionApi;
import be.sysa.quartz.initializer.fixtures.assertions.definition.GroupDefinitionApiAssert;
import be.sysa.quartz.initializer.fixtures.assertions.definition.JobDefinitionApiAssert;
import be.sysa.quartz.initializer.fixtures.assertions.definition.TriggerDefinitionApiAssert;
import be.sysa.quartz.initializer.fixtures.jobs.MyTestJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import static be.sysa.quartz.initializer.fixtures.ScheduleAssert.assertThat;
import static java.time.ZoneOffset.UTC;
import static org.mockito.Mockito.when;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

@ExtendWith(MockitoExtension.class)
public class ScheduleExporterTest {

    private static final String GROUP_NAME = "group";
    private static final String JOB_NAME = "job";
    private static final String TRIGGER_NAME = "trigger";
    private final JobKey jobKey = JobKey.jobKey(JOB_NAME, GROUP_NAME);
    @Mock
    ScheduleAccessor scheduleAccessor;
    private ScheduleExporter scheduleExporter;

    @BeforeEach
    void setUp() {
        scheduleExporter = new ScheduleExporter(scheduleAccessor);
    }

    @Test
    public void readExistingSchedule() {
        // Setup Schedule
        JobDetail jobDetail = newJob(MyTestJob.class)
                .withIdentity(jobKey)
                .requestRecovery(true)
                .withDescription("description")
                .storeDurably(true)
                .build();
        Trigger trigger = newTrigger()
                .forJob(jobKey)
                .withIdentity(TRIGGER_NAME, GROUP_NAME)
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(10, 1)
                        .inTimeZone(TimeZone.getTimeZone(UTC)))
                .usingJobData("key1", 10)
                .build();
        List<Trigger> triggers = List.of(trigger);

        when(scheduleAccessor.getJobGroupNames()).thenReturn(List.of(GROUP_NAME));
        when(scheduleAccessor.getJobsInGroup(GROUP_NAME)).thenReturn(Set.of(jobKey));
        when(scheduleAccessor.getJobDetail(jobKey)).thenReturn(jobDetail);
        when(scheduleAccessor.getTriggersOfJob(jobKey)).thenAnswer(t -> triggers);

        // Execute read from Schedule
        ScheduleDefinitionApi scheduleDefinitionApi = scheduleExporter.readExistingSchedule();

        // Assert ScheduleDefinition
        GroupDefinitionApiAssert groupAssert = assertThat(scheduleDefinitionApi).hasGroups(GROUP_NAME).hasGroup(GROUP_NAME);
        JobDefinitionApiAssert jobAssert = groupAssert.hasJobs(JOB_NAME).hasJob(JOB_NAME);
        TriggerDefinitionApiAssert triggerAssert = jobAssert.jobClass(MyTestJob.class)
                .durable(true)
                .recover(true)
                .hasJobData(Collections.emptyMap())
                .descriptionContains("description")
                .hasTriggers(TRIGGER_NAME)
                .hasTrigger(TRIGGER_NAME);

        triggerAssert.misfireExecution(false)
                .cronExpressions("0 1 10 ? * *")
                .priority(Trigger.DEFAULT_PRIORITY)
                .hasJobData(Map.of("key1", 10))
                .noDescription();
    }
}
