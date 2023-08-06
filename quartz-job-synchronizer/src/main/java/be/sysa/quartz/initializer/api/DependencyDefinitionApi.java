package be.sysa.quartz.initializer.api;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Map;

@Value
@Builder(toBuilder = true)
public class DependencyDefinitionApi {
    String name;

    @JsonProperty("child-job-name")
    String childJobName;

    @JsonProperty("child-job-group")
    String childJobGroup;

    @JsonProperty("not-before")
    String notBefore;

    Integer priority;

    @JsonProperty("seconds-delay")
    Integer secondsDelay;

    @JsonProperty("ignore-parent-error")
    boolean ignoreParentError;

    @Singular("jobData")
    @JsonProperty("job-data-map")
    Map<String, Object> jobDataMap;

}
