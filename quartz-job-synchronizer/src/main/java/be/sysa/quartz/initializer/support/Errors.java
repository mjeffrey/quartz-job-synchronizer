package be.sysa.quartz.initializer.support;

public enum Errors {
    JOB_CLASS_NOT_A_JOB,
    CRON_EXPRESSION_INVALID,
    DUPLICATE_GROUPS,
    TIMEZONE_INVALID;

    public ScheduleDefinitionException toException(String format, Object... parameters) {
        return new ScheduleDefinitionException(this, null, format, parameters);
    }

    public ScheduleDefinitionException toException(Exception cause, String format, Object... parameters) {
        return new ScheduleDefinitionException(this, cause, format, parameters);
    }

}
