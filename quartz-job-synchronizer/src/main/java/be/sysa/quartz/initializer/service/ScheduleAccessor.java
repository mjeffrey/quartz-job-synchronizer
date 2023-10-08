package be.sysa.quartz.initializer.service;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.List;
import java.util.Set;

/**
 * This class provides access to the scheduler in order to perform various operations on jobs and triggers.
 * It is a wrapper around the Scheduler delegate
 */
@Value
@Slf4j
@AllArgsConstructor
public class ScheduleAccessor {
    Scheduler scheduler;

    // ====================== Get jobs/triggers ======================
    /**
     * Retrieves the names of all job groups from the scheduler.
     *
     * @return A list of strings representing the names of all job groups.
     */
    @SneakyThrows
    List<String> getJobGroupNames() {
        return scheduler.getJobGroupNames();
    }

    /**
     * Retrieves the job keys of all jobs in the specified group from the scheduler.
     *
     * @param groupName The name of the job group.
     * @return A set of JobKey objects representing the job keys of all jobs in the specified group.
     */
    @SneakyThrows
    Set<JobKey> getJobsInGroup(String groupName) {
        return scheduler.getJobKeys(GroupMatcher.groupEquals(groupName));
    }

    /**
     * Retrieves the JobDetail object of the specified job key from the scheduler.
     *
     * @param jobToUpdate The JobKey object representing the job key of the job to retrieve.
     * @return The JobDetail object of the specified job key.
     */
    @SneakyThrows
    JobDetail getJobDetail(JobKey jobToUpdate) {
        return scheduler.getJobDetail(jobToUpdate);
    }

    /**
     * Retrieves a list of Trigger objects associated with the specified job key from the scheduler.
     *
     * @param jobKey The JobKey object representing the job key of the job to retrieve triggers for.
     * @return A list of Trigger objects associated with the specified job key.
     */
    @SneakyThrows
    List<? extends Trigger> getTriggersOfJob(JobKey jobKey) {
        return scheduler.getTriggersOfJob(jobKey);
    }

    /**
     * Retrieves the Trigger object associated with the specified trigger key from the scheduler.
     *
     * @param triggerKey The TriggerKey object representing the trigger key of the trigger to retrieve.
     * @return The Trigger object associated with the specified trigger key.
     */
    @SneakyThrows
    Trigger getTrigger(TriggerKey triggerKey) {
        return scheduler.getTrigger(triggerKey);
    }

    /**
     * Retrieves the state of the Trigger associated with the specified trigger key from the Scheduler.
     *
     * @param triggerKey The TriggerKey object representing the trigger key of the trigger to retrieve the state from.
     * @return The state of the Trigger associated with the specified trigger key. Possible values are:
     *  - NONE: No state is associated with the Trigger key.
     *  - NORMAL: The Trigger key is in the normal state.
     *  - PAUSED: The Trigger key is in the paused state.
     *  - COMPLETE: The Trigger key is in the complete state.
     *  - ERROR: The Trigger key is in the error state.
     *  - BLOCKED: The Trigger key is in the blocked state.
     */
    @SneakyThrows
    Trigger.TriggerState getTriggerState(TriggerKey triggerKey) {
        return scheduler.getTriggerState(triggerKey);
    }


    // ====================== Add jobs/triggers ======================

    /**
     * Adds a JobDetail to the Scheduler, along with any associated Triggers. The JobDetail specifies the class
     * of Job to be executed, and the Triggers specify when and how often the Job should be executed.
     *
     * @param jobDetail The JobDetail object representing the Job to be added to the Scheduler.
     */
    @SneakyThrows
    void addJob(JobDetail jobDetail) {
        scheduler.addJob(jobDetail, true, true);
    }

    /**
     * Reschedules a Trigger in the Scheduler with a new CronTrigger. This method updates the existing Trigger
     * with the provided TriggerKey with the new CronTrigger provided.
     *
     * @param triggerKey  The TriggerKey object representing the existing Trigger to be rescheduled.
     * @param cronTrigger The CronTrigger object representing the new CronTrigger to be used for rescheduling.
     */
    @SneakyThrows
    void rescheduleJob(TriggerKey triggerKey, Trigger cronTrigger) {
        log.info("rescheduling trigger {}", triggerKey);
        scheduler.rescheduleJob(triggerKey, cronTrigger);
    }

    /**
     * Schedules a Job with the provided Trigger in the Scheduler. This method associates the Job with the Trigger
     * and adds it to the Scheduler for execution.
     *
     * @param trigger The Trigger object representing the schedule for executing the Job.
     */
    @SneakyThrows
    public void scheduleJob(Trigger trigger) {
        log.info("Scheduling trigger {} {}",
                trigger.getKey(),
                trigger.getDescription());
        scheduler.scheduleJob(trigger);
    }


    // ====================== Delete jobs/triggers ======================

    /**
     * Deletes all jobs in the specified group. This method retrieves all jobs in the given group and deletes each of them one by one.
     *
     * @param group The name of the group from which jobs should be deleted.
     */
    void deleteJobsInGroup(String group) {
        getJobsInGroup(group).forEach(this::deleteJob);
    }

    /**
     * Deletes a Job (including its associated triggers) from the Scheduler.
     *
     * @param jobKey The JobKey object representing the Job to be deleted from the Scheduler.
     */
    @SneakyThrows
    void deleteJob(JobKey jobKey) {
        log.info("Deleting job (and triggers) {}", jobKey);
        scheduler.deleteJob(jobKey);
    }

    /**
     * Unschedules a Trigger in the Scheduler. This method removes the Trigger with the provided TriggerKey
     * from the Scheduler.
     *
     * @param triggerKey The TriggerKey object representing the Trigger to be unscheduled.
     */
    @SneakyThrows
    void unscheduleJob(TriggerKey triggerKey) {
        log.info("Deleting trigger {}", triggerKey);
        scheduler.unscheduleJob(triggerKey);
    }

    /**
     * Retrieves the name of the Scheduler.
     *
     * @return A String representing the name of the Scheduler.
     */
    @SneakyThrows
    public String getSchedulerName() {
        return scheduler.getSchedulerName();
    }

    /**
     * Retrieves the ListenerManager of the Scheduler. The ListenerManager is responsible for managing
     * the registered listeners for the Scheduler.
     *
     * @return The ListenerManager object of the Scheduler.
     */
    @SneakyThrows
    public ListenerManager getListenerManager() {
        return scheduler.getListenerManager();
    }
}
