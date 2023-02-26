package be.sysa.quartz.initializer.service;

import be.sysa.quartz.initializer.model.GroupDefinition;
import be.sysa.quartz.initializer.model.JobDefinition;
import be.sysa.quartz.initializer.model.ScheduleDefinition;
import be.sysa.quartz.initializer.model.TriggerDefinition;
import be.sysa.quartz.initializer.support.ComparisonSupport;
import be.sysa.quartz.initializer.support.SetUtils;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static be.sysa.quartz.initializer.support.SetUtils.intersection;
import static be.sysa.quartz.initializer.support.SetUtils.minus;

@Value
@Slf4j
public class JobSynchronizer {
    ScheduleAccessor scheduleAccessor;

    public JobSynchronizer(Scheduler scheduler) {
        this.scheduleAccessor = new ScheduleAccessor(scheduler);
    }

    @SneakyThrows
    public void synchronizeSchedule(ScheduleDefinition scheduleDefinition) {
        deleteGroupsSpecified(scheduleDefinition.getGroupsToDelete());

        List<String> existingGroups = scheduleAccessor.getJobGroupNames();
        Map<String, GroupDefinition> newGroups = scheduleDefinition.getGroups();
        removeUnusedGroups(existingGroups, newGroups);
        addNewGroups(existingGroups, newGroups);
        updateExistingGroupsIfNeeded(existingGroups, newGroups);
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

    @SneakyThrows
    private void updateTriggerIfChanged(TriggerDefinition triggerDefinition) {
        TriggerKey triggerKey = triggerDefinition.getTriggerKey();
        Trigger trigger = scheduleAccessor.getTrigger(triggerKey);
        if (ComparisonSupport.triggerChanged(trigger, triggerDefinition)) {
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

    private void addNewTriggers(Set<TriggerKey> existingTriggers, Map<TriggerKey, TriggerDefinition> newTriggers) {
        Set<TriggerKey> triggersToAdd = minus(newTriggers.keySet(), existingTriggers);
        triggersToAdd.forEach(triggerKey -> addTrigger(newTriggers.get(triggerKey)));
    }

    private void removeObsoleteTriggers(Set<TriggerKey> existingTriggers, Map<TriggerKey, TriggerDefinition> newTriggers) {
        Set<TriggerKey> removeTriggers = minus(existingTriggers, newTriggers.keySet());
        deleteTriggers(removeTriggers);
    }

    @SneakyThrows
    private void deleteTriggers(Set<TriggerKey> removeTriggers) {
        removeTriggers.forEach(scheduleAccessor::deleteTrigger);
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
    private void addTrigger(TriggerDefinition triggerDefinition) {
        CronTrigger cronTrigger = createTrigger(triggerDefinition);
        log.info("Adding trigger: {} {}", cronTrigger.getKey(), cronTrigger.getCronExpression());
        scheduleAccessor.scheduleJob(cronTrigger);
    }

    private static CronTrigger createTrigger(TriggerDefinition triggerDefinition) {
        JobDataMap triggerJobData = new JobDataMap(triggerDefinition.getJobDataMap());
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(triggerDefinition.getCronExpression())
                .inTimeZone(triggerDefinition.getTimeZone());
        if (triggerDefinition.isMisfireExecution()) {
            scheduleBuilder.withMisfireHandlingInstructionFireAndProceed();
        }

        String description = triggerDefinition.getDescription();
        return TriggerBuilder.newTrigger()
                .forJob(triggerDefinition.getJobkey())
                .usingJobData(triggerJobData)
                .withDescription(description)
                .withIdentity(triggerDefinition.getTriggerKey())
                .withSchedule(scheduleBuilder)
                .withPriority(triggerDefinition.getPriority())
                .build();
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
