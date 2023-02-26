package be.sysa.quartz.initializer.support;

import be.sysa.quartz.initializer.model.TriggerDefinition;
import org.junit.jupiter.api.Test;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import java.util.Map;
import java.util.TimeZone;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.quartz.TriggerBuilder.newTrigger;

public class ComparisonSupportTest {
    private static final String GROUP_NAME = "group";
    private static final String JOB_NAME = "job";
    private static final TimeZone TIMEZONE = TimeZone.getTimeZone(UTC);
    private static final String DESCRIPTION = "description";
    private final JobKey jobKey = JobKey.jobKey(JOB_NAME, GROUP_NAME);
    private final TriggerKey triggerKey = TriggerKey.triggerKey(JOB_NAME, GROUP_NAME);

    @Test
    public void jobDefinitionChanged() {
        TriggerDefinition triggerDefinition = TriggerDefinition.builder()
                .jobkey(jobKey)
                .cronExpression("0 1 10 ? * *")
                .timeZone(TIMEZONE)
                .priority(10)
                .description(DESCRIPTION)
                .misfireExecution(true)
                .triggerKey(triggerKey)
                .jobDataMap(Map.of(
                        "datakey1", "valueX",
                        "datakey2", 0.123))
                .build();

        Trigger trigger = newTrigger()
                .forJob(jobKey)
                .withIdentity(triggerKey)
                .withPriority(10)
                .withDescription(DESCRIPTION)
                .withSchedule(CronScheduleBuilder.dailyAtHourAndMinute(10, 1)
                        .inTimeZone(TIMEZONE)
                        .withMisfireHandlingInstructionFireAndProceed())
                .usingJobData("datakey1", "valueX")
                .usingJobData("datakey2", 0.123)
                .build();

        assertThat(ComparisonSupport.triggerChanged(trigger, triggerDefinition))
                .describedAs("\nTrigger    %s \nDefinition %s", trigger, triggerDefinition)
                .isFalse();
    }

    @Test
    public void triggerChanged() {
    }
}
