package be.sysa.quartz.initializer.model;

import lombok.Builder;
import lombok.Value;
import org.quartz.JobKey;

import java.util.Map;

/**
 * Represents a Quartz Job group definition.
 */
@Builder(toBuilder = true)
@Value
public class GroupDefinition {

    String name;

    Map<JobKey, JobDefinition> jobs;
}
