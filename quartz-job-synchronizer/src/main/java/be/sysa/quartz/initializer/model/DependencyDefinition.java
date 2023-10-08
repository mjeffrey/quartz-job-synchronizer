package be.sysa.quartz.initializer.model;


import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.quartz.JobKey;

import java.util.Map;

/**
 * A class representing a dependency definition for a job. A Dependency is a child job will be triggered after
 * the parent job has executed. It will be scheduled either immediately or after a time.
 */
@Value
@Builder(toBuilder = true)
public class DependencyDefinition {
    String name;
    JobKey childJob;
    int priority;
    int secondsDelay;
    ZonedTime notBefore;
    boolean parentErrorIgnored;

    @Singular("jobData")
    Map<String, Object> jobDataMap;
}
