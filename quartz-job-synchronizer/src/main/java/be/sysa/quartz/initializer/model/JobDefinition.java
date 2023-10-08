package be.sysa.quartz.initializer.model;


import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import org.quartz.Job;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a definition of a job.
 *
 * <p>{@code JobDefinition} is a value class with the following properties:
 * <ul>
 *   <li>{@code jobKey} - The key that uniquely identifies the job.</li>
 *   <li>{@code jobClass} - The class of the job to be executed.</li>
 *   <li>{@code description} - The description of the job (optional).</li>
 *   <li>{@code recover} - Indicates whether the job is recoverable or not.</li>
 *   <li>{@code durable} - Indicates whether the job should be persisted or not.</li>
 *   <li>{@code triggers} - A map of trigger keys to trigger definitions, representing the triggers associated with the job.</li>
 *   <li>{@code dependencies} - A list of dependency definitions, representing the dependencies of the job (optional).</li>
 *   <li>{@code jobDataMap} - A map of job data, containing additional data associated with the job (optional).</li>
 * </ul>
 *
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * JobDefinition jobDefinition = JobDefinition.builder()
 *     .jobKey(jobKey)
 *     .jobClass(MyJob.class)
 *     .description("This is a job definition")
 *     .recover(true)
 *     .durable(true)
 *     .trigger(triggerKey, triggerDefinition)
 *     .jobData("key1", "value1")
 *     .jobData("key2", 123)
 *     .build();
 * }</pre>
 */
@Value
@Builder(toBuilder = true)
public class JobDefinition {

    @NonNull JobKey  jobKey;
    @NonNull Class<? extends Job> jobClass;

    String description;

    boolean recover;

    boolean durable;

    @NonNull
    Map<TriggerKey, TriggerDefinition> triggers;

    @NonNull
    @Builder.Default
    List<DependencyDefinition> dependencies = Collections.emptyList();

    @Singular("jobData")
    Map<String, Object> jobDataMap;
}
