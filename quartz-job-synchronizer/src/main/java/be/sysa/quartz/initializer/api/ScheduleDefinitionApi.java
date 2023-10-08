package be.sysa.quartz.initializer.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

/**
 * Represents a schedule definition API.
 * This class provides methods to retrieve schedule groups and options.
 */
@Value
@Builder(toBuilder = true)
public class ScheduleDefinitionApi {

    @Singular
    @JsonProperty("schedule")
    List<GroupDefinitionApi> groups;

    @JsonProperty("options")
    OptionsDefinitionApi options;

    /**
     * Retrieves the mandatory options.
     *
     * @return The OptionsDefinitionApi object representing the mandatory options. If the options are not set, a new empty OptionsDefinitionApi object is returned.
     */
    @JsonIgnore
    public OptionsDefinitionApi getMandatoryOptions() {
        return options == null ? OptionsDefinitionApi.builder().build() : options;
    }
}
