package be.sysa.quartz.initializer.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class ScheduleDefinitionApi {

    @Singular
    @JsonProperty("schedule")
    List<GroupDefinitionApi> groups;

    @JsonProperty("options")
    OptionsDefinitionApi options;

    @JsonIgnore
    public OptionsDefinitionApi getMandatoryOptions() {
        return options == null ? OptionsDefinitionApi.builder().build() : options;
    }
}
