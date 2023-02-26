package be.sysa.quartz.initializer.support;

import be.sysa.quartz.initializer.model.TriggerDefinition;
import org.junit.jupiter.api.Test;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobKey;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

import java.util.Collections;
import java.util.Map;
import java.util.TimeZone;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.quartz.TriggerBuilder.newTrigger;

public class TriggerDifferencerTest {
    private static final String GROUP_NAME = "MyGroup";
    private static final String JOB_NAME = "MyJob";
    private static final String TRIGGER_NAME = "MyTrigger";
    private static final TimeZone TIMEZONE = TimeZone.getTimeZone(UTC);
    private static final String DESCRIPTION = "description";
    private final JobKey jobKey = JobKey.jobKey(JOB_NAME, GROUP_NAME);
    private final TriggerKey triggerKey = TriggerKey.triggerKey(TRIGGER_NAME, GROUP_NAME);

    TriggerBuilder<CronTrigger> existingTrigger = newTrigger()
            .forJob(jobKey)
            .withIdentity(triggerKey)
            .withPriority(11)
            .withDescription(DESCRIPTION)
            .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(10, 1)
                    .inTimeZone(TIMEZONE)
                    .withMisfireHandlingInstructionFireAndProceed())
            ;
    TriggerDefinition newTrigger = TriggerDefinition.builder()
            .jobkey(jobKey)
            .triggerKey(triggerKey)
            .cronExpression("0 1 10 ? * *")
            .timeZone(TIMEZONE)
            .priority(11)
            .description(DESCRIPTION)
            .misfireExecution(true)
            .build();

    @Test
    public void triggerUnchanged() {
        assertThat(Differencer.difference(existingTrigger.build(), newTrigger).logDifferences()).hasSize(0);
    }

    @Test
    public void triggerKeyChanged() {
        TriggerDefinition anotherTrigger = newTrigger.toBuilder().triggerKey(TriggerKey.triggerKey(TRIGGER_NAME, "anotherTrigger")).build();
        assertDifference(anotherTrigger, Difference.Type.TRIGGER_KEY);
    }

    @Test
    public void cronExpressionChanged() {
        TriggerDefinition anotherTrigger = newTrigger.toBuilder().cronExpression("* * 9 ? * *").build();
        assertDifference(anotherTrigger, Difference.Type.CRON);
    }

    @Test
    public void timeZoneChanged() {
        TriggerDefinition anotherTrigger = newTrigger.toBuilder().timeZone(TimeZone.getTimeZone("Europe/Paris")).build();
        assertDifference(anotherTrigger, Difference.Type.TIMEZONE);
    }

    @Test
    public void misFireExecutionChanged() {
        TriggerDefinition anotherTrigger = newTrigger.toBuilder().misfireExecution(false).build();
        assertDifference(anotherTrigger, Difference.Type.MISFIRE_IGNORE); // TODO add the opposite case
    }

    @Test
    public void priorityChanged() {
        TriggerDefinition anotherTrigger = newTrigger.toBuilder().priority(13).build();
        assertDifference(anotherTrigger, Difference.Type.PRIORITY);
    }

    @Test
    public void jobDataMapDeleted() {
        existingTrigger.usingJobData("datakey1", "valueX");
        TriggerDefinition anotherTrigger = newTrigger.toBuilder().jobDataMap(Collections.emptyMap()).build();
        assertDifference(anotherTrigger, Difference.Type.DATA_MAP_DELETED);
    }

    @Test
    public void jobDataMapRemovedElement() {
        existingTrigger.usingJobData("datakey1", "valueX");
        existingTrigger.usingJobData("datakey2", 2.53);
        TriggerDefinition anotherTrigger = newTrigger.toBuilder().jobDataMap(Map.of("x", "y")).build();
        assertDifference(anotherTrigger, Difference.Type.DATA_MAP_ENTRIES_CHANGE);
    }

    @Test
    public void jobDataMapAddNew() {
        TriggerDefinition anotherTrigger = newTrigger.toBuilder().jobDataMap(Map.of("x", "y")).build();
        assertDifference(anotherTrigger, Difference.Type.DATA_MAP_NEW);
    }

    @Test
    public void jobDataChangeValue() {
        existingTrigger.usingJobData("datakey1", "valueX");
        TriggerDefinition anotherTrigger = newTrigger.toBuilder().jobDataMap(Map.of("datakey1", 1000)).build();
        assertDifference(anotherTrigger, Difference.Type.DATA_MAP_ENTRY_VALUE_CHANGED);
    }

    private void assertDifference(TriggerDefinition anotherTrigger, Difference.Type type) {
        assertThat(Differencer.difference(existingTrigger.build(), anotherTrigger).logDifferences()).hasSize(1)
                .extracting(Difference::getType).contains(type);
    }

}
