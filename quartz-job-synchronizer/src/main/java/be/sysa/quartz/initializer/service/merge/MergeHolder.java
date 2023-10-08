package be.sysa.quartz.initializer.service.merge;

import be.sysa.quartz.initializer.model.GroupDefinition;
import be.sysa.quartz.initializer.model.ScheduleDefinition;
import lombok.AllArgsConstructor;

import java.util.ArrayList;

@AllArgsConstructor
public class MergeHolder {

    private ScheduleDefinition definition;

    public void addGroup(GroupDefinition groupValue) {
        definition = definition.toBuilder().group(groupValue.getName(), groupValue).build();
    }

    public void addGroupsToDelete(ArrayList<String> groupsToDelete) {
        definition= definition.toBuilder().groupsToDelete(groupsToDelete).build();
    }

    public ScheduleDefinition build() {
        return definition;
    }
}
