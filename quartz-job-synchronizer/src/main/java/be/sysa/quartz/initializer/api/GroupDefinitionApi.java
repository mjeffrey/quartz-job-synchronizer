package be.sysa.quartz.initializer.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

/**
 * This class represents a definition for a group derived from an external representation (yaml).
 * It contains the name of the group and a list of job definitions associated with the group.
 */
@Builder(toBuilder = true)
@Value
public class GroupDefinitionApi {

    @JsonProperty("group")
    String name;

    @Singular
    List<JobDefinitionApi> jobs;

}
