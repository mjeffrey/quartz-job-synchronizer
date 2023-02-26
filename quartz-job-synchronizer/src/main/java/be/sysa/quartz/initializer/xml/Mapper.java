package be.sysa.quartz.initializer.xml;

import be.sysa.quartz.initializer.api.GroupDefinitionApi;
import be.sysa.quartz.initializer.api.JobDefinitionApi;
import be.sysa.quartz.initializer.api.ScheduleDefinitionApi;
import be.sysa.quartz.initializer.api.TriggerDefinitionApi;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.quartz.JobKey;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Mapper {
    public static ScheduleDefinitionApi toModel(ScheduleDataXml scheduleDataXml) {
        return toModel(scheduleDataXml.getSchedule());
    }

    private static ScheduleDefinitionApi toModel(ScheduleXml scheduleXml) {
        ScheduleDefinitionApi.ScheduleDefinitionApiBuilder scheduleBuilder = ScheduleDefinitionApi.builder();
        Set<JobXml> jobs = assertNoDuplicates(scheduleXml.getJobs());
        Set<CronTriggerXml> triggers = assertNoDuplicates(
                scheduleXml.getTriggers().stream().map(TriggerXml::getCronTriggerXml)
                        .collect(Collectors.toList())
        );

        Map<String, List<JobXml>> groups = jobs.stream().collect(
                Collectors.groupingBy(JobXml::getGroup, LinkedHashMap::new, Collectors.toList()));

        for (Map.Entry<String, List<JobXml>> jobEntries : groups.entrySet()) {
            scheduleBuilder.group(
                    GroupDefinitionApi.builder()
                            .name(jobEntries.getKey())
                            .jobs(toJobs(jobEntries.getValue(), triggers))
                            .build()
            );
        }
        return scheduleBuilder.build();
    }

    private static Collection<JobDefinitionApi> toJobs(Collection<JobXml> jobs, Collection<CronTriggerXml> triggers) {
        Map<JobKey, List<CronTriggerXml>> jobTriggers = triggers.stream().collect(
                Collectors.groupingBy(Mapper::jobKey,  LinkedHashMap::new, Collectors.toList()));
        return jobs.stream().map(job -> JobDefinitionApi.builder()
                .durable(job.isDurability())
                .recover(job.isRecover())
                .jobClass(job.getJobClass())
                .name(job.getName())
                .jobDataMap(toJobData(job.getJobDataMap()))
                .triggers(toTriggers(jobTriggers.get(JobKey.jobKey(job.getName(), job.getGroup()))))
                .build()
        ).collect(Collectors.toList());
    }

    private static Collection<TriggerDefinitionApi> toTriggers(List<CronTriggerXml> triggers) {
        return triggers.stream()
                .map(Mapper::toTrigger).collect(Collectors.toList());
    }

    private static TriggerDefinitionApi toTrigger(CronTriggerXml cronTriggerXml) {
        return TriggerDefinitionApi.builder()
                .name(cronTriggerXml.getName())
                .triggerGroup(cronTriggerXml.getGroup())
                .description(cronTriggerXml.getDescription())
                .timeZone(cronTriggerXml.getTimeZone())
                .misfireExecution("MISFIRE_INSTRUCTION_FIRE_ONCE_NOW"
                        .equals(cronTriggerXml.getMisfireInstruction()))
                .cronExpression(cronTriggerXml.getCronExpression())
                .priority(cronTriggerXml.getPriority())
                .jobDataMap(toJobData(cronTriggerXml.getJobDataMap()))
                .build();
    }


    private static Map<String, Object> toJobData(List<JobDataEntryXml> mapEntries) {
        return mapEntries == null
                ? Collections.emptyMap()
                : mapEntries.stream().collect(Collectors.toMap(JobDataEntryXml::getKey, JobDataEntryXml::getValue));
    }

    private static <T> Set<T> assertNoDuplicates(List<T> list) {
        HashSet<T> hashSet = new LinkedHashSet<>(list);
        if (list.size() != hashSet.size()) {
            throw new IllegalArgumentException(String.format("Duplicate found: %s", list));
        }
        return hashSet;
    }

    private static JobKey jobKey(CronTriggerXml t) {
        return JobKey.jobKey(t.getJobName(), t.getJobGroup());
    }
}
