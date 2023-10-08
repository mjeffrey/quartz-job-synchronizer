package be.sysa.quartz.initializer.support;

import be.sysa.quartz.initializer.model.TriggerDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.*;

import java.util.Map;
import java.util.TimeZone;

import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * See also DifferencerTest
 */
public class ComparisonSupportTest {
    private static final TimeZone TIMEZONE = TimeZone.getTimeZone(UTC);
    private static final String DESCRIPTION = "description";

    private static final String GROUP_NAME = "group";
    private static final JobKey JOB_KEY = JobKey.jobKey("job", GROUP_NAME);

    private final CronScheduleBuilder CRON_SCHEDULE = CronScheduleBuilder.dailyAtHourAndMinute(10, 1)
            .inTimeZone(TIMEZONE)
            .withMisfireHandlingInstructionFireAndProceed();
    private  final TriggerKey TRIGGER_KEY = TriggerKey.triggerKey("trigger", GROUP_NAME);

    private  final TriggerDefinition TRIGGER_DEFINITION = TriggerDefinition.builder()
            .jobkey(JOB_KEY)
            .cronExpression("0 1 10 ? * *")
            .timeZone(TIMEZONE)
            .priority(10)
            .description(DESCRIPTION)
            .misfireExecution(true)
            .triggerKey(TRIGGER_KEY)
            .jobDataMap(Map.of(
                    "datakey1", "valueX",
                    "datakey2", 0.123)).build();
    private TriggerBuilder<? extends Trigger> triggerBuilder;

    @Test
    @DisplayName("If the trigger matches all criteria it has not changed.")
    public void triggerDefinitionUnChanged() {
        with(CRON_SCHEDULE).assertUnchanged();
    }

    @Test
    @DisplayName("Cron change is treated as changed.")
    public void triggerChangedSinceItIsNotCron() {
        ScheduleBuilder<? extends Trigger> scheduleBuilderDifferentTime = CronScheduleBuilder.dailyAtHourAndMinute(13, 1)
                .inTimeZone(TIMEZONE)
                .withMisfireHandlingInstructionFireAndProceed();
        with(scheduleBuilderDifferentTime).assertChanged();
    }
    @Test
    @DisplayName("Cron timezone is treated as changed.")
    public void triggerChangedTimezone() {
        with(CRON_SCHEDULE.inTimeZone(TimeZone.getTimeZone("Australia/Sydney"))).assertChanged();
    }

    @Test
    @DisplayName("Cron misfire changed treated as changed.")
    public void triggerMisfireChanged() {
        with(CRON_SCHEDULE.withMisfireHandlingInstructionIgnoreMisfires()).assertChanged();
        with(CRON_SCHEDULE.withMisfireHandlingInstructionDoNothing()).assertChanged();
    }

    @Test
    @DisplayName("Any existing trigger that is not cron is treated as changed.")
    public void cronChanged() {
        with(SimpleScheduleBuilder.repeatHourlyForever()).assertChanged();
    }

    private TriggerBuilder<? extends Trigger> triggerBuilder(ScheduleBuilder<? extends Trigger> scheduleBuilder) {
        return newTrigger()
                .forJob(JOB_KEY)
                .withIdentity(TRIGGER_KEY)
                .withPriority(10)
                .withDescription(DESCRIPTION)
                .withSchedule(scheduleBuilder)
                .usingJobData("datakey1", "valueX")
                .usingJobData("datakey2", 0.123);
    }

    ComparisonSupportTest with(ScheduleBuilder<? extends Trigger> scheduleBuilder){
        triggerBuilder = triggerBuilder(scheduleBuilder);
        return this;
    }

    private void assertUnchanged() {
        Trigger trigger = triggerBuilder.build();
        assertThat(ComparisonSupport.triggerChanged(trigger, TRIGGER_DEFINITION))
                .describedAs("\nTrigger    %s \nDefinition %s", triggerBuilder, TRIGGER_DEFINITION)
                .isFalse();
    }

    private void assertChanged() {
        Trigger trigger = triggerBuilder.build();
        assertThat(ComparisonSupport.triggerChanged(trigger, TRIGGER_DEFINITION))
                .describedAs("\nTrigger    %s \nDefinition %s", trigger, TRIGGER_DEFINITION)
                .isTrue();
    }

}
