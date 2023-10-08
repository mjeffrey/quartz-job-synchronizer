package be.sysa.quartz.initializer.api;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class represents a job definition in an API.
 *
 * It contains various properties such as the job name, job class, description, recovery flag, durability flag, triggers,
 * dependencies, and a map of job data.
 *
 * The job name, job class, and description are represented as strings, while the recovery and durability flags are
 * represented as booleans.
 *
 * The triggers and dependencies are represented as lists of TriggerDefinitionApi and DependencyDefinitionApi objects
 * respectively.
 *
 * The job data map is represented as a map of string keys to object values.
 *
 * This class provides getter methods for the triggers and dependencies lists, which return an empty list if the lists
 * are null.
 *
 * This class is immutable and can be built using the builder pattern provided by Lombok's @Builder annotation.
 */
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

    @Singular
    List<DependencyDefinitionApi> dependencies;

    @Singular("jobData")
    @JsonProperty("job-data-map")
    Map<String, Object> jobDataMap;

    /**
     * Retrieves a list of trigger definitions.
     *
     * @return a list of trigger definitions, or an empty list if no triggers are present
     */
    public List<TriggerDefinitionApi> getTriggers() {
        return triggers == null ? Collections.emptyList() : triggers;
    }

    /**
     * Returns the list of dependent jobs.
     *
     * @return the list of dependencies. If there are no dependencies, an empty list is returned.
     */
    public List<DependencyDefinitionApi> getDependencies() {
        return dependencies == null ? Collections.emptyList() : dependencies;
    }
}
