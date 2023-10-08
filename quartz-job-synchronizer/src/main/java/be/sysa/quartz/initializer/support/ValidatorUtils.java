package be.sysa.quartz.initializer.support;

import org.quartz.CronExpression;

import java.time.ZoneId;

/**
 * This class provides utility methods for validating cron expressions and timezones.
 */
public class ValidatorUtils {

    /**
     * Asserts whether a given cron expression is valid.
     *
     * @param cronExpression the cron expression to validate
     * @throws IllegalArgumentException if the cron expression is invalid
     */
    public static void assertCronExpressionValid(String cronExpression){
        try {
            new CronExpression(cronExpression);
        } catch (Exception e) {
            throw Errors.CRON_EXPRESSION_INVALID.toException( e, "Could not parse CRON expression %s", cronExpression);
        }
    }

    /**
     * Asserts whether a given timezone is valid.
     *
     * @param timezone the timezone expression to validate
     * @throws IllegalArgumentException if the timezone is invalid
     */
    public static void assertTimezoneValid(String timezone){
        try {
            if (timezone!=null) ZoneId.of(timezone);
        } catch (Exception e) {
            throw Errors.TIMEZONE_INVALID.toException("Could not parse TimeZone expression %s", timezone);
        }

    }

}
