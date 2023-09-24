package be.sysa.quartz.initializer.service;

import be.sysa.quartz.initializer.model.GroupDefinition;
import be.sysa.quartz.initializer.model.JobDefinition;
import be.sysa.quartz.initializer.model.ScheduleDefinition;
import be.sysa.quartz.initializer.service.merge.MergeHolder;
import be.sysa.quartz.initializer.support.Errors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.util.*;

/**
 * A class that merges schedules {@link ScheduleDefinition} from multiple files into one.
 */
@Slf4j
public class ScheduleMerger {

    private ScheduleDefinition merged;
    private Set<String> allGroups = new HashSet<>();
    private Set<JobKey> allJobKeys = new HashSet<>();
    private Set<TriggerKey> allTriggerKeys = new HashSet<>();

    /**
     * Merge a list of schedules into one. Errors on duplicates
     *
     * @param scheduleDefinitions the original list
     * @return the schedules combined into one.
     */
    public static ScheduleDefinition merge(List<ScheduleDefinition> scheduleDefinitions) {
        ScheduleMerger merger = new ScheduleMerger();
        return merger.mergeSchedules(scheduleDefinitions);
    }

    @SneakyThrows
    private ScheduleDefinition mergeSchedules(List<ScheduleDefinition> scheduleDefinitions) {
        if (scheduleDefinitions.isEmpty()) {
            throw Errors.NO_SCHEDULES.toException("No Schedules read");
        }
        assertNoDuplicates(scheduleDefinitions);
        for (ScheduleDefinition scheduleDefinition : scheduleDefinitions) {
            merged = merge(scheduleDefinition);
        }
        return merged;
    }

    private ScheduleDefinition merge(ScheduleDefinition schedule) {
        if (merged == null) {
            return schedule;
        }
        MergeHolder mergeHolder = new MergeHolder(merged);
        mergeGroupDeletions(mergeHolder, schedule.getGroupsToDelete());
        mergeGroups(mergeHolder, schedule.getGroups().values());
        return mergeHolder.build();
    }


    private void mergeGroups(MergeHolder mergeHolder, Collection<GroupDefinition> groups) {
        for (GroupDefinition group : groups) {
            mergeHolder.addGroup(group);
        }
    }

    private void mergeGroupDeletions(MergeHolder mergeHolder, List<String> additional) {
        List<String> existing = merged.getGroupsToDelete();
        Set<String> set1 = new LinkedHashSet<>(existing);
        Set<String> set2 = new LinkedHashSet<>(additional);
        set1.addAll(set2);
        mergeHolder.addGroupsToDelete(new ArrayList<>(set1));
    }

    private void assertNoDuplicates(List<ScheduleDefinition> scheduleDefinitions) {
        for (ScheduleDefinition scheduleDefinition : scheduleDefinitions) {
            for (GroupDefinition groupDefinition : scheduleDefinition.getGroups().values()) {
                assertNotDuplicated(groupDefinition);
                for (JobDefinition jobDefinition : groupDefinition.getJobs().values()) {
                    assertNotDuplicated(jobDefinition.getJobKey());
                    for (TriggerKey triggerKey : jobDefinition.getTriggers().keySet()) {
                        assertNotDuplicated(triggerKey);
                    }
                }
            }
        }
    }

    private void assertNotDuplicated(GroupDefinition groupDefinition) {
        String groupName = groupDefinition.getName();
        boolean added = allGroups.add(groupName);
        if (!added) {
            throw Errors.DUPLICATE_GROUPS.toException("Duplicate Group found %s", groupName);
        }
    }

    private void assertNotDuplicated(JobKey jobKey) {
        boolean added = allJobKeys.add(jobKey);
        if (!added) {
            throw Errors.DUPLICATE_JOB.toException("Duplicate Job found %s", jobKey);
        }
    }

    private void assertNotDuplicated(TriggerKey triggerKey) {
        boolean added = allTriggerKeys.add(triggerKey);
        if (!added) {
            throw Errors.DUPLICATE_TRIGGER.toException("Duplicate Trigger found %s", triggerKey);
        }
    }

}
