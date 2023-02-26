package be.sysa.quartz.initializer.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

@Builder(toBuilder = true)
@Value
public class OptionsDefinitionApi {

    @Singular("groupToDelete")
    @JsonProperty("groups-to-delete")
    List<String> groupsToDelete;

}
