package be.sysa.quartz.initializer.model;


import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import org.quartz.JobKey;

import java.util.Map;

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
