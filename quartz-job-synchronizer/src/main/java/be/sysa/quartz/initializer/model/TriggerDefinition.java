package be.sysa.quartz.initializer.model;


import lombok.Builder;
import lombok.Singular;
import lombok.ToString;
import lombok.Value;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.util.Map;
import java.util.TimeZone;

/**
 * Represents a definition of a trigger for a job.
 *
 */
@Value
@Builder(toBuilder = true)
public class TriggerDefinition {

    JobKey jobkey;

    TriggerKey triggerKey;

    boolean misfireExecution;

    String description;

    int priority;

    String cronExpression;

    @ToString.Exclude
    TimeZone timeZone;

    @Singular("jobData")
    Map<String, Object> jobDataMap;

}
