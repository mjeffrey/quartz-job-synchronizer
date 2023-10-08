package be.sysa.quartz.initializer.api;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Map;

/**
 * Represents the definition of a dependency for a job.
 * <p>
 * This class is used to define the dependency between jobs,
 * allowing one job to depend on the completion of another job.
 * </p>
 *
 * <p>
 * The DependencyDefinitionApi class is immutable, meaning that once
 * an instance is created, its state cannot be modified. The class
 * provides a builder pattern to conveniently create instances with
 * desired values for the properties.
 * </p>
 *
 * <p>
 * The properties of the DependencyDefinitionApi class include the following:
 * </p>
 * <ul>
 *   <li>name - The name of the dependency</li>
 *   <li>childJobName - The name of the child job</li>
 *   <li>childJobGroup - The group of the child job</li>
 *   <li>notBefore - A date string representing the time when the child job should not run before</li>
 *   <li>priority - The priority of the dependency</li>
 *   <li>secondsDelay - The number of seconds to delay the child job from running</li>
 *   <li>ignoreParentError - A flag indicating whether to ignore errors in the parent job</li>
 *   <li>jobDataMap - A map containing additional data for the job</li>
 * </ul>
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>{@code
 * DependencyDefinitionApi dependency = DependencyDefinitionApi.builder()
 *     .name("Dependency1")
 *     .childJobName("ChildJob")
 *     .childJobGroup("ChildGroup")
 *     .notBefore("2022-01-01T00:00:00")
 *     .priority(1)
 *     .secondsDelay(60)
 *     .ignoreParentError(false)
 *     .jobDataMap(Collections.singletonMap("key", "value"))
 *     .build();
 * }</pre>
 */
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
