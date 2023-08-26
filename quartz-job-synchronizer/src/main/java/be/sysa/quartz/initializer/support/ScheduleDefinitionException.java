package be.sysa.quartz.initializer.support;

import lombok.Getter;

public class ScheduleDefinitionException extends RuntimeException {
    @Getter
    private final Errors errorCode;

    ScheduleDefinitionException(Errors errorCode, Exception cause, String format, Object... parameters) {
        super(errorCode + " : " + String.format(format, parameters), cause);
        this.errorCode = errorCode;
    }
}
