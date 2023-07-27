package be.sysa.quartz.initializer.model;

import be.sysa.quartz.initializer.api.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerKey;

import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Mapper {
    private static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone(ZoneOffset.UTC);
    private static final int MAX_TRIGGER_NAME_LENGTH = 250;

    public static ScheduleDefinition toModel(ScheduleDefinitionApi scheduleDefinitionApi) {
        return ScheduleDefinition.builder()
                .groups(toGroups(scheduleDefinitionApi))
                .groupsToDelete(scheduleDefinitionApi.getMandatoryOptions().getGroupsToDelete())
                .build();
    }

    private static Map<String, GroupDefinition> toGroups(ScheduleDefinitionApi scheduleDefinitionApi) {
        return scheduleDefinitionApi.getGroups().stream()
                .map(Mapper::toGroup)
                .collect(Collectors.toMap(GroupDefinition::getName, Function.identity()));
    }

    private static GroupDefinition toGroup(GroupDefinitionApi groupDefinitionApi) {
        return GroupDefinition.builder()
                .jobs(toJobs(groupDefinitionApi))
                .name(groupDefinitionApi.getName())
                .build();
    }

    private static Map<JobKey, JobDefinition> toJobs(GroupDefinitionApi groupDefinitionApi) {
        return groupDefinitionApi.getJobs().stream()
                .map(job -> toJob(groupDefinitionApi.getName(), job))
                .collect(Collectors.toMap(JobDefinition::getJobKey, Function.identity()));
    }

    private static JobDefinition toJob(String groupName, JobDefinitionApi job) {
        JobKey jobKey = new JobKey(job.getName(), groupName);
        return JobDefinition.builder()
                .jobKey(jobKey)
                .jobClass(getJobClass(job))
                .recover(recoverJob(job))
                .durable(durable(job))
                .jobDataMap(nullSafeJobDataMap(job.getJobDataMap()))
                .description(job.getDescription())
                .triggers(toTriggers(jobKey, job))
                .dependencies(toDependencies(jobKey, job))
                .build();
    }

    private static Map<TriggerKey, TriggerDefinition> toTriggers(JobKey jobKey, JobDefinitionApi job) {
        Map<TriggerKey, TriggerDefinition> triggerMap = new LinkedHashMap<>();
        job.getTriggers().forEach(t -> triggerMap.putAll(toTriggers(jobKey, t)));
        return triggerMap;
    }

    private static Map<TriggerKey, TriggerDefinition> toTriggers(JobKey jobKey, TriggerDefinitionApi triggerDefinitionApi) {
        Map<TriggerKey, TriggerDefinition> triggerMap = new LinkedHashMap<>();

        int scheduleNumber = 1;
        for (String cronExpression : triggerDefinitionApi.getCronExpressions()) {
            TriggerDefinition triggerDefinition = toTrigger(jobKey, triggerDefinitionApi, cronExpression, scheduleNumber++);
            triggerMap.put(triggerDefinition.getTriggerKey(), triggerDefinition);
        }
        return triggerMap;
    }

    private static TriggerKey triggerKey(JobKey jobKey, TriggerDefinitionApi triggerDefinitionApi, int scheduleNumber) {
        String triggerName = triggerName(jobKey.getName(), triggerDefinitionApi.getName(), scheduleNumber);
        String triggerGroup = Objects.requireNonNullElse(triggerDefinitionApi.getTriggerGroup(), jobKey.getGroup());
        return new TriggerKey(triggerName, triggerGroup);
    }

    private static TriggerDefinition toTrigger(JobKey jobKey, TriggerDefinitionApi triggerDefinitionApi, String cronExpression, int scheduleNumber) {
        TriggerKey triggerKey = triggerKey(jobKey, triggerDefinitionApi, scheduleNumber);
        return TriggerDefinition.builder()
                .triggerKey(triggerKey)
                .description(triggerDefinitionApi.getDescription())
                .jobkey(jobKey)
                .misfireExecution(misfireHandling(triggerDefinitionApi))
                .priority(priority(triggerDefinitionApi.getPriority()))
                .timeZone(defaultTimeZone(triggerDefinitionApi.getTimeZone()))
                .cronExpression(cronExpression)
                .jobDataMap(nullSafeJobDataMap(triggerDefinitionApi.getJobDataMap()))
                .build();
    }

    private static Map<String, Object> nullSafeJobDataMap(Map<String, Object> jobDataMap) {
        return jobDataMap == null ? Collections.emptyMap() : jobDataMap;
    }

    @SneakyThrows
    private static Class<? extends Job> getJobClass(JobDefinitionApi jobDefinition) {
        String jobClass = jobDefinition.getJobClass();
        try {
            return castToJobClass(Class.forName(jobClass));
        } catch (ClassNotFoundException e) {
            throw new ClassNotFoundException("Class '" + jobClass + "' could not be found. Jobs must exist in the classpath when configuring them.");
        }
    }

    @SuppressWarnings("unchecked")
    private static Class<? extends Job> castToJobClass(Class<?> jobClass) {
        if (!Job.class.isAssignableFrom(jobClass)) {
            throw new IllegalArgumentException(
                    "Job class '" + jobClass.getName() + "' must implement the Job interface.");
        }
        return (Class<? extends Job>) jobClass;
    }

    private static TimeZone defaultTimeZone(String timezone) {
        return timezone == null ? DEFAULT_TIMEZONE : TimeZone.getTimeZone(timezone);
    }

    private static String triggerName(@NonNull String jobName, @NonNull String triggerName, int scheduleNumber) {
        if (StringUtils.startsWith(triggerName, jobName + ".")) {
            return triggerName;
        }
        String name = jobName + "." + triggerName + "." + scheduleNumber;
        return StringUtils.truncate(name, MAX_TRIGGER_NAME_LENGTH);
    }

    private static List<DependencyDefinition> toDependencies(JobKey jobKey, JobDefinitionApi job) {
        return job.getDependencies().stream().map(dependency -> toDependency(jobKey, dependency)).collect(Collectors.toList());
    }

    private static DependencyDefinition toDependency(JobKey parentJobKey, DependencyDefinitionApi dependency) {
        String childGroup = Objects.requireNonNullElse(dependency.getChildJobGroup(), parentJobKey.getGroup());
        return DependencyDefinition.builder()
                .name(dependency.getName())
                .jobDataMap(nullSafeJobDataMap(dependency.getJobDataMap()))
                .childJob(JobKey.jobKey(dependency.getChildJobName(), childGroup))
                .priority(priority(dependency.getPriority()))
                .secondsDelay(Objects.requireNonNullElse(dependency.getSecondsDelay(), 0))
                .parentErrorIgnored(dependency.isIgnoreParentError())
                .build();
    }

    /**
     * @param jobDefinition The job definition
     * @return if Job should be stored recovered if it fails. Default false
     */
    public static boolean recoverJob(JobDefinitionApi jobDefinition) {
        Boolean recover = jobDefinition.getRecover();
        return recover != null && recover;
    }

    private static boolean durable(JobDefinitionApi jobDefinition) {
        Boolean durable = jobDefinition.getDurable();
        return durable == null || durable;
    }

    public static boolean misfireHandling(TriggerDefinitionApi triggerDefinition) {
        Boolean misfireExecution = triggerDefinition.getMisfireExecution();
        return misfireExecution != null && misfireExecution;
    }

    public static int priority(Integer priority) {
        return Objects.requireNonNullElse(priority, Trigger.DEFAULT_PRIORITY);
    }
}
