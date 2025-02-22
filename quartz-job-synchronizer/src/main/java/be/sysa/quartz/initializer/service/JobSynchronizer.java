package be.sysa.quartz.initializer.service;

import be.sysa.quartz.initializer.model.*;
import be.sysa.quartz.initializer.support.ComparisonSupport;
import be.sysa.quartz.initializer.support.CronDescriptionService;
import be.sysa.quartz.initializer.support.SetUtils;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.Trigger.TriggerState;

import java.util.*;
import java.util.stream.Collectors;

import static be.sysa.quartz.initializer.support.SetUtils.intersection;
import static be.sysa.quartz.initializer.support.SetUtils.minus;

/**
 * A class that synchronizes the schedule of jobs based on the given {@link ScheduleDefinition}.
 */
@Value
@Slf4j
public class JobSynchronizer {
    ScheduleAccessor scheduleAccessor;

    /**
     * Creates a new instance of JobSynchronizer with the given Scheduler.
     *
     * @param scheduler the Scheduler used for job scheduling and management
     */
    public JobSynchronizer(Scheduler scheduler) {
        this.scheduleAccessor = new ScheduleAccessor(scheduler);
    }

    /**
     * Synchronizes the schedule based on the provided ScheduleDefinition.
     * Deletes specified groups, removes unused groups, adds new groups, and updates existing groups if needed.
     * Additionally, adds dependent job triggers for all jobs in the provided groups.
     *
     * @param scheduleDefinition the ScheduleDefinition containing the schedule information
     */
    @SneakyThrows
    public void synchronizeSchedule(ScheduleDefinition scheduleDefinition) {
        deleteGroupsSpecified(scheduleDefinition.getGroupsToDelete());

        List<String> existingGroups = scheduleAccessor.getJobGroupNames();
        Map<String, GroupDefinition> newGroups = scheduleDefinition.getGroups();
        removeUnusedGroups(existingGroups, newGroups);
        addNewGroups(existingGroups, newGroups);
        updateExistingGroupsIfNeeded(existingGroups, newGroups);

        // Add all the dependent Jobs
        scheduleDefinition.getGroups().values()
                .stream().map(value -> value.getJobs().values())
                .flatMap(Collection::stream)
                .forEach(this::addDependentJobTriggers);
    }

    private void deleteGroupsSpecified(List<String> groupsToDelete) {
        groupsToDelete.forEach(scheduleAccessor::deleteJobsInGroup);
    }

    private void addNewGroups(List<String> existingGroups, Map<String, GroupDefinition> groupDefinitions) {
        Set<String> groupsToAdd = SetUtils.minus(groupDefinitions.keySet(), existingGroups);
        for (String group : groupsToAdd) {
            GroupDefinition groupDefinition = groupDefinitions.get(group);
            for (JobDefinition jobDefinition : groupDefinition.getJobs().values()) {
                addJob(jobDefinition);
            }
        }
    }

    private void updateExistingGroupsIfNeeded(List<String> existingGroups, Map<String, GroupDefinition> definedGroups) {
        Set<String> groupsToUpdate = SetUtils.intersection(definedGroups.keySet(), existingGroups);
        for (String groupToUpdate : groupsToUpdate) {
            handleGroupUpdated(definedGroups.get(groupToUpdate));
        }
    }

    @SneakyThrows
    private void handleGroupUpdated(GroupDefinition definedGroup) {
        String groupName = definedGroup.getName();
        Set<JobKey> existingJobs = scheduleAccessor.getJobsInGroup(groupName);

        Map<JobKey, JobDefinition> newJobs = definedGroup.getJobs();
        removeObsoleteJobs(existingJobs, newJobs);
        addNewJobs(existingJobs, newJobs);
        updateExistingJobsIfNeeded(existingJobs, newJobs);
    }


    private void removeUnusedGroups(List<String> existingGroups, Map<String, GroupDefinition> newGroups) {
        Set<String> groupsToRemove = SetUtils.minus(existingGroups, newGroups.keySet());
        groupsToRemove.forEach(scheduleAccessor::deleteJobsInGroup);
    }

    @SneakyThrows
    private void updateExistingJobsIfNeeded(Set<JobKey> existingJobs, Map<JobKey, JobDefinition> newJobs) {
        Set<JobKey> updateJobs = intersection(existingJobs, newJobs.keySet());
        for (JobKey jobToUpdate : updateJobs) {
            JobDetail jobDetail = scheduleAccessor.getJobDetail(jobToUpdate);
            JobDefinition jobDefinition = newJobs.get(jobToUpdate);
            if (ComparisonSupport.jobDefinitionChanged(jobDetail, jobDefinition)) {
                updateJob(jobDefinition);
            } else {
                updateJobTriggersIfNeeded(jobDefinition);
            }
        }
    }

    @SneakyThrows
    private void updateJob(JobDefinition jobDefinition) {
        JobDetail jobDetail = createJob(jobDefinition);
        log.info("Updating Job: {}", jobDefinition);
        scheduleAccessor.addJob(jobDetail);
        updateJobTriggersIfNeeded(jobDefinition);
    }

    @SneakyThrows
    private void updateJobTriggersIfNeeded(JobDefinition jobDefinition) {
        List<? extends Trigger> existingTriggers = scheduleAccessor.getTriggersOfJob(jobDefinition.getJobKey());
        Set<TriggerKey> existingTriggerKeys = existingTriggers.stream().map(Trigger::getKey).collect(Collectors.toUnmodifiableSet());

        Map<TriggerKey, TriggerDefinition> newTriggers = jobDefinition.getTriggers();
        removeObsoleteTriggers(existingTriggerKeys, newTriggers);
        addNewTriggers(existingTriggerKeys, newTriggers);
        updateExistingTriggersIfNeeded(existingTriggerKeys, newTriggers);
    }

    private void updateExistingTriggersIfNeeded(Set<TriggerKey> existingTriggers, Map<TriggerKey, TriggerDefinition> newTriggers) {
        Set<TriggerKey> triggersToUpdate = intersection(existingTriggers, newTriggers.keySet());
        triggersToUpdate.forEach(triggerKey -> updateTriggerIfChanged(newTriggers.get(triggerKey)));
    }

    private static CronTrigger createTrigger(TriggerDefinition triggerDefinition) {
        JobDataMap triggerJobData = new JobDataMap(triggerDefinition.getJobDataMap());
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(triggerDefinition.getCronExpression())
                .inTimeZone(triggerDefinition.getTimeZone());
        if (triggerDefinition.isMisfireExecution()) {
            scheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
        }else{
            scheduleBuilder.withMisfireHandlingInstructionDoNothing();
        }

        String description = getDescription(triggerDefinition);
        return TriggerBuilder.newTrigger()
                .forJob(triggerDefinition.getJobkey())
                .usingJobData(triggerJobData)
                .withDescription(description)
                .withIdentity(triggerDefinition.getTriggerKey())
                .withSchedule(scheduleBuilder)
                .withPriority(triggerDefinition.getPriority())
                .build();
    }

    private void addNewTriggers(Set<TriggerKey> existingTriggers, Map<TriggerKey, TriggerDefinition> newTriggers) {
        Set<TriggerKey> triggersToAdd = minus(newTriggers.keySet(), existingTriggers);
        triggersToAdd.forEach(triggerKey -> addTrigger(newTriggers.get(triggerKey)));
    }

    private static String getDescription(TriggerDefinition triggerDefinition) {
        return Optional.ofNullable(triggerDefinition.getDescription())
                .orElse(CronDescriptionService.instance()
                        .getDescription(triggerDefinition.getCronExpression()));
    }

    private void removeObsoleteTriggers(Set<TriggerKey> existingTriggers, Map<TriggerKey, TriggerDefinition> newTriggers) {
        Set<TriggerKey> removeTriggers = minus(existingTriggers, newTriggers.keySet());
        deleteTriggers(removeTriggers);
    }

    @SneakyThrows
    private void deleteTriggers(Set<TriggerKey> removeTriggers) {
        removeTriggers.forEach(scheduleAccessor::unscheduleJob);
    }

    @SneakyThrows
    private JobDetail addJob(JobDefinition jobDefinition) {
        JobDetail jobDetail = createJob(jobDefinition);
        log.info("Adding Job: {}", jobDefinition);
        scheduleAccessor.addJob(jobDetail);
        for (TriggerDefinition trigger : jobDefinition.getTriggers().values()) {
            addTrigger(trigger);
        }
        return jobDetail;
    }

    private static JobDetail createJob(JobDefinition jobDefinition) {
        return JobBuilder
                .newJob(jobDefinition.getJobClass())
                .usingJobData(new JobDataMap(jobDefinition.getJobDataMap()))
                .withDescription(jobDefinition.getDescription())
                .storeDurably(jobDefinition.isDurable())
                .requestRecovery(jobDefinition.isRecover())
                .withIdentity(jobDefinition.getJobKey())
                .build();
    }

    @SneakyThrows
    private void updateTriggerIfChanged(TriggerDefinition triggerDefinition) {
        TriggerKey triggerKey = triggerDefinition.getTriggerKey();
        Trigger trigger = scheduleAccessor.getTrigger(triggerKey);
        if (ComparisonSupport.triggerChanged(trigger, triggerDefinition) || triggerInError(triggerKey)) {
            CronTrigger cronTrigger = createTrigger(triggerDefinition);
            scheduleAccessor.rescheduleJob(triggerKey, cronTrigger);
            if (triggerDefinition.isMisfireExecution() && trigger.getNextFireTime().before(new Date())) {
                log.warn("Trigger {} for job {} has missed its fire time. Scheduling the job now",
                        triggerKey.getName(),
                        trigger.getJobKey().getName());
                scheduleAccessor.scheduleJob(TriggerBuilder.newTrigger()
                        .withIdentity(triggerDefinition.getTriggerKey().getName(), "startup")
                        .forJob(trigger.getJobKey())
                        .startNow()
                        .build());
            }
        }
    }

    private boolean triggerInError(TriggerKey triggerKey) {
        TriggerState triggerState = scheduleAccessor.getTriggerState(triggerKey);
        return triggerState == TriggerState.ERROR;
    }

    @SneakyThrows
    private void addDependentJobTriggers(JobDefinition jobDefinition) {
        JobKey parentJobKey = jobDefinition.getJobKey();
        ListenerManager listenerManager = scheduleAccessor.getListenerManager();

        for (DependencyDefinition dependency : jobDefinition.getDependencies()) {
            JobKey childJobKey = dependency.getChildJob();
            log.info("Adding job listener, on completion of job {}, the job {} will run",
                    parentJobKey,
                    childJobKey
            );
            listenerManager.addJobListener(DependentJobListener.create(dependency),
                    jobKey -> jobKey.equals(parentJobKey));
        }
    }

    @SneakyThrows
    private void addTrigger(TriggerDefinition triggerDefinition) {
        CronTrigger cronTrigger = createTrigger(triggerDefinition);
        String description = CronDescriptionService.instance().getDescription(cronTrigger.getCronExpression());
        log.info("Adding trigger: {} cron='{}'",
                cronTrigger.getKey(),
                cronTrigger.getCronExpression());
        if (description != null) {
            log.info("===> {}, zone={}", description, triggerDefinition.getTimeZone().toZoneId());
        }
        scheduleAccessor.scheduleJob(cronTrigger);
    }

    private void addNewJobs(Set<JobKey> existingJobs, Map<JobKey, JobDefinition> newJobs) {
        Set<JobKey> jobsToAdd = minus(newJobs.keySet(), existingJobs);
        jobsToAdd.forEach(job -> addJob(newJobs.get(job)));
    }


    private void removeObsoleteJobs(Set<JobKey> existingJobs, Map<JobKey, JobDefinition> newJobs) {
        Set<JobKey> jobsToDelete = minus(existingJobs, newJobs.keySet());
        jobsToDelete.forEach(scheduleAccessor::deleteJob);
    }


}
