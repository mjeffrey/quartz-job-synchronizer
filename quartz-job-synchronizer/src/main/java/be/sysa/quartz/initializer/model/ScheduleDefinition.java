package be.sysa.quartz.initializer.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Represents a schedule definition.
 */
@Builder(toBuilder = true)
@Value
public class ScheduleDefinition {

    @Singular
    Map<String, GroupDefinition> groups;

    List<String> groupsToDelete;

    /**
     * Returns the list of groups to delete.
     * If the groupsToDelete variable is null, it returns an empty list.
     * Otherwise, it returns the groupsToDelete list.
     *
     * @return the list of groups to delete
     */
    public List<String> getGroupsToDelete() {
        return groupsToDelete == null ? Collections.emptyList() : groupsToDelete;
    }
}
