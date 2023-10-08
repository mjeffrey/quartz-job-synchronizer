package be.sysa.quartz.initializer.support;

/**
 * The Errors enum represents different types of errors that can occur in the application.
 *
 * <p>
 * The enum provides methods to convert the current object to a ScheduleDefinitionException object.
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>
 * ScheduleDefinitionException exception = Errors.JOB_CLASS_NOT_A_JOB.toException("Job class is not a valid job");
 * throw exception;
 * </pre>
 *
 * @see ScheduleDefinitionException
 */
public enum Errors {
    /**
     * This error indicates that the specified job Class does not implement Job.
     */
    JOB_CLASS_NOT_A_JOB,
    /**
     * This error indicates that the CRON expression is invalid.
     */
    CRON_EXPRESSION_INVALID,
    /**
     * This error indicates that the Duplicate Groups were found in the Schedule.
     */
    DUPLICATE_GROUP,

    /**
     * This error indicates that a Duplicate Jobs were found in the Schedule.
     */
    DUPLICATE_JOB,
    /**
     * This error indicates that a Duplicate Trigger was found in the Schedule.
     */
    DUPLICATE_TRIGGER,
    /**
     * This error indicates that the Timezone was not valid. Use the long for e,g, Europe/Brussels or UTC
     */
    TIMEZONE_INVALID,

    /**
     * If there were no schedules defined
     */
    NO_SCHEDULES;

    /**
     * Converts the current object to a ScheduleDefinitionException.
     *
     * @param format          the format string for the exception message
     * @param parameters      the parameters to be used in the format string
     * @return a new ScheduleDefinitionException object
     */
    public ScheduleDefinitionException toException(String format, Object... parameters) {
        return new ScheduleDefinitionException(this, null, format, parameters);
    }

    /**
     * Converts the current object to a ScheduleDefinitionException with an exception cause.
     *
     * @param cause             the exception cause
     * @param format            the format string for the exception message
     * @param parameters        the parameters to be used in the format string
     * @return a new ScheduleDefinitionException object with the specified cause
     */
    public ScheduleDefinitionException toException(Exception cause, String format, Object... parameters) {
        return new ScheduleDefinitionException(this, cause, format, parameters);
    }

}
