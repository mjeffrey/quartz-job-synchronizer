package be.sysa.quartz.initializer.api;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder(toBuilder = true)
public class JobDefinitionApi {
    @JsonProperty("job-name")
    String name;

    @JsonProperty("job-class")
    String jobClass;

    String description;

    Boolean recover;

    Boolean durable;

    @Singular
    List<TriggerDefinitionApi> triggers;

    @Singular("jobData")
    @JsonProperty("job-data-map")
    Map<String, Object> jobDataMap;

}
