package be.sysa.quartz.initializer.support;

import org.quartz.CronExpression;

import java.time.ZoneId;

public class ValidatorUtils {

    public static void assertCronExpressionValid(String cronExpression){
        try {
            new CronExpression(cronExpression);
        } catch (Exception e) {
            throw Errors.CRON_EXPRESSION_INVALID.toException( e, "Could not parse CRON expression %s", cronExpression);
        }
    }

    public static void assertTimezoneValid(String timezone){
        try {
            if (timezone!=null) ZoneId.of(timezone);
        } catch (Exception e) {
            throw Errors.TIMEZONE_INVALID.toException("Could not parse TimeZone expression %s", timezone);
        }

    }

}
