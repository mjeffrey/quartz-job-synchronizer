package be.sysa.quartz.initializer.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Builder(toBuilder = true)
@Value
public class GroupDefinitionApi {

    @JsonProperty("group")
    String name;

    @Singular
    List<JobDefinitionApi> jobs;

}
