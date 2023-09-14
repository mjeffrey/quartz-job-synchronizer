package be.sysa.quartz.initializer.support;

import lombok.Getter;

/**
 * Exception thrown when there is an error in the definition of a schedule.
 */
@Getter
public class ScheduleDefinitionException extends RuntimeException {
    /**
     * Represents an error code. Used to identify what cause the error.
     */
    private final Errors errorCode;

    ScheduleDefinitionException(Errors errorCode, Exception cause, String format, Object... parameters) {
        super(errorCode + " : " + String.format(format, parameters), cause);
        this.errorCode = errorCode;
    }
}
