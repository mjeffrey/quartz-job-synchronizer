package be.sysa.quartz.initializer.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

/**
 * This class represents an API for defining options.
 * The only current options are a list of groups to delete. By defaults groups are not deleted
 */
@Builder(toBuilder = true)
@Value
public class OptionsDefinitionApi {

    @Singular("groupToDelete")
    @JsonProperty("groups-to-delete")
    List<String> groupsToDelete;

}
