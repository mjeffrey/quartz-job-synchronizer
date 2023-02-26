package be.sysa.quartz.initializer.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Builder(toBuilder = true)
@Value
public class ScheduleDefinition {

    @Singular
    Map<String, GroupDefinition> groups;

    List<String> groupsToDelete;

    public List<String> getGroupsToDelete() {
        return groupsToDelete == null ? Collections.emptyList() : groupsToDelete;
    }
}
