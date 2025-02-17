package be.sysa.quartz.initializer.support;

import be.sysa.quartz.initializer.model.TriggerDefinition;
import be.sysa.quartz.initializer.support.Difference.Type;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.util.TimeZone;
import java.util.stream.Stream;

import static be.sysa.quartz.initializer.support.Difference.Type.MISFIRE_EXECUTE_NOW;
import static be.sysa.quartz.initializer.support.Difference.Type.MISFIRE_IGNORE;
import static java.time.ZoneOffset.UTC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.quartz.TriggerBuilder.newTrigger;

public class TriggerMisfireDifferenceTest {
    private static final String GROUP_NAME = "MyGroup";
    private static final String JOB_NAME = "MyJob";
    private static final String TRIGGER_NAME = "MyTrigger";
    private static final TimeZone TIMEZONE = TimeZone.getTimeZone(UTC);
    private static final String DESCRIPTION = "description";
    private static final JobKey jobKey = JobKey.jobKey(JOB_NAME, GROUP_NAME);
    private static final TriggerKey triggerKey = TriggerKey.triggerKey(TRIGGER_NAME, GROUP_NAME);

    @ParameterizedTest
    @MethodSource("testDifferencesData")
    void testDifferences(CronTrigger triggerInSchedule, TriggerDefinition newDefinition, Type difference) {
        if (difference == null) {
            assertNoDifference(triggerInSchedule, newDefinition);
        } else {
            assertDifference(triggerInSchedule, newDefinition, difference);
        }
    }

    private void assertNoDifference(CronTrigger triggerInSchedule, TriggerDefinition newDefinition) {
        assertThat(Differencer.difference(triggerInSchedule, newDefinition).logDifferences()).isEmpty();
    }

    private void assertDifference(CronTrigger existingTrigger, TriggerDefinition newDefinition, Type difference) {
        assertThat(Differencer.difference(existingTrigger, newDefinition).logDifferences()).hasSize(1)
                .extracting(Difference::getType).contains(difference);
    }

    private static Stream<Arguments> testDifferencesData() {

        TriggerDefinition misFireHandlingTrue;
        TriggerDefinition misFireHandlingFalse;
        CronTrigger ignoreMisfires;
        CronTrigger fireAndProceed;
        CronTrigger doNothing;

        misFireHandlingTrue = TriggerDefinition.builder()
                .jobkey(jobKey)
                .triggerKey(triggerKey)
                .cronExpression("0 1 10 ? * *")
                .timeZone(TIMEZONE)
                .priority(11)
                .description(DESCRIPTION)
                .misfireExecution(true)
                .build();

        misFireHandlingFalse = misFireHandlingTrue.toBuilder().misfireExecution(false).build();

        ignoreMisfires = triggerFor(CronScheduleBuilder.dailyAtHourAndMinute(10, 1).inTimeZone(TIMEZONE).withMisfireHandlingInstructionIgnoreMisfires());
        fireAndProceed = triggerFor(CronScheduleBuilder.dailyAtHourAndMinute(10, 1).inTimeZone(TIMEZONE).withMisfireHandlingInstructionFireAndProceed());
        doNothing = triggerFor(CronScheduleBuilder.dailyAtHourAndMinute(10, 1).inTimeZone(TIMEZONE).withMisfireHandlingInstructionDoNothing());

        return Stream.of(
                Arguments.of(ignoreMisfires, misFireHandlingTrue, MISFIRE_EXECUTE_NOW),
                Arguments.of(fireAndProceed, misFireHandlingTrue, null),
                Arguments.of(doNothing, misFireHandlingTrue, MISFIRE_EXECUTE_NOW),
                Arguments.of(ignoreMisfires, misFireHandlingFalse, null),
                Arguments.of(fireAndProceed, misFireHandlingFalse, MISFIRE_IGNORE),
                Arguments.of(doNothing, misFireHandlingFalse, null)
        );
    }

    private static CronTrigger triggerFor(CronScheduleBuilder cronScheduleBuilder) {
        return newTrigger()
                .forJob(jobKey)
                .withIdentity(triggerKey)
                .withPriority(11)
                .withDescription("DESCRIPTION")
                .withSchedule(cronScheduleBuilder)
                .build();
    }

}
