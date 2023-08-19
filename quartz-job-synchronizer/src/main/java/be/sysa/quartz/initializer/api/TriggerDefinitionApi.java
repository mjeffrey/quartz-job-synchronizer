package be.sysa.quartz.initializer.api;


import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;
import java.util.Map;

@Value
@Builder(toBuilder = true)
public class TriggerDefinitionApi {

    @JsonAlias({"trigger-name", "name"}) // allow "name"
    @JsonProperty("trigger-name")
    String name;

    @JsonProperty("trigger-group")
    String triggerGroup;

    @JsonProperty("time-zone")
    String timeZone;

    @Singular
    @JsonProperty("expressions")
    List<String> cronExpressions;

    String description;

    @JsonProperty("misfire-execution")
    Boolean misfireExecution;

    Integer priority;

    @Singular("jobData")
    @JsonProperty("job-data-map")
    Map<String, Object> jobDataMap;

}
