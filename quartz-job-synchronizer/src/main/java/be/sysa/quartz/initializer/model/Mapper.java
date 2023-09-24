package be.sysa.quartz.initializer.model;

import be.sysa.quartz.initializer.api.*;
import be.sysa.quartz.initializer.support.Errors;
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

import static be.sysa.quartz.initializer.support.ValidatorUtils.assertCronExpressionValid;
import static be.sysa.quartz.initializer.support.ValidatorUtils.assertTimezoneValid;

/**
 *
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Mapper {
    private static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone(ZoneOffset.UTC);
    private static final int MAX_TRIGGER_NAME_LENGTH = 250;

    /**
     * Converts a ScheduleDefinitionApi object to a ScheduleDefinition object.
     *
     * @param scheduleDefinitionApi The ScheduleDefinitionApi object to be converted.
     * @return The converted ScheduleDefinition object.
     */
    public static ScheduleDefinition toModel(ScheduleDefinitionApi scheduleDefinitionApi) {
        return ScheduleDefinition.builder()
                .groups(toGroups(scheduleDefinitionApi))
                .groupsToDelete(scheduleDefinitionApi.getMandatoryOptions().getGroupsToDelete())
                .build();
    }

    private static Map<String, GroupDefinition> toGroups(ScheduleDefinitionApi scheduleDefinitionApi) {
        return scheduleDefinitionApi.getGroups().stream()
                .map(Mapper::toGroup)
                .collect(Collectors.toMap(GroupDefinition::getName, Function.identity(),
                        (group1, group2) -> {
                            throw Errors.DUPLICATE_GROUP.toException("Group '%s' appears twice in the schedule.", group1.getName());}
                        ));
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

        List<String> cronExpressions = triggerDefinitionApi.getCronExpressions();
        TriggerSuffixProvider triggerSuffixProvider = new TriggerSuffixProvider(cronExpressions.size());
        assertTimezoneValid(triggerDefinitionApi.getTimeZone());
        for (String cronExpression : cronExpressions) {
            assertCronExpressionValid(cronExpression);
            TriggerDefinition triggerDefinition = toTrigger(jobKey, triggerDefinitionApi, cronExpression, triggerSuffixProvider.increment());
            triggerMap.put(triggerDefinition.getTriggerKey(), triggerDefinition);
        }
        return triggerMap;
    }

    private static class TriggerSuffixProvider {
        final int numExpressions;
        int current;

        private TriggerSuffixProvider(int numExpressions) {
            this.numExpressions = numExpressions;
            this.current = 1;
        }

        private String increment() {
            return numExpressions == 1 ? null : "." + current++;
        }
    }

    private static TriggerDefinition toTrigger(JobKey jobKey, TriggerDefinitionApi triggerDefinitionApi, String cronExpression, String suffix) {
        TriggerKey triggerKey = triggerKey(jobKey, triggerDefinitionApi, suffix);
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

    private static TriggerKey triggerKey(JobKey jobKey, TriggerDefinitionApi triggerDefinitionApi, String suffix) {
        String triggerName = triggerName(triggerDefinitionApi.getName(), suffix);
        String triggerGroup = Objects.requireNonNullElse(triggerDefinitionApi.getTriggerGroup(), jobKey.getGroup());
        return new TriggerKey(triggerName, triggerGroup);
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
            throw Errors.JOB_CLASS_NOT_A_JOB.toException("Job class '%s' must implement the Job interface.", jobClass.getName());
        }
        return (Class<? extends Job>) jobClass;
    }

    private static TimeZone defaultTimeZone(String timezone) {
        return timezone == null ? DEFAULT_TIMEZONE : TimeZone.getTimeZone(timezone);
    }

    private static String triggerName(@NonNull String triggerName, String suffix) {
        if (suffix == null){
            return triggerName;
        }
        if (StringUtils.endsWith(triggerName, suffix)) {
            return triggerName;
        }
        String name = triggerName + suffix;
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
                .notBefore(parseZonedTime(dependency.getNotBefore()))
                .secondsDelay(Objects.requireNonNullElse(dependency.getSecondsDelay(), 0))
                .parentErrorIgnored(dependency.isIgnoreParentError())
                .build();
    }

    private static ZonedTime parseZonedTime(String string) {
        return string == null ? null : ZonedTime.parse(string);
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

    /**
     * Determines if we handle misfires
     *
     * @param triggerDefinition The trigger definition
     * @return True if the job associated with the trigger should be stored and recovered if it fails, false otherwise.
     */
    public static boolean misfireHandling(TriggerDefinitionApi triggerDefinition) {
        Boolean misfireExecution = triggerDefinition.getMisfireExecution();
        return misfireExecution != null && misfireExecution;
    }

    /**
     * Determines the priority of a task.
     *
     * @param priority The priority of the task. Can be null.
     * @return The priority of the task. Returns the default priority if the given priority is null.
     */
    public static int priority(Integer priority) {
        return Objects.requireNonNullElse(priority, Trigger.DEFAULT_PRIORITY);
    }
}
