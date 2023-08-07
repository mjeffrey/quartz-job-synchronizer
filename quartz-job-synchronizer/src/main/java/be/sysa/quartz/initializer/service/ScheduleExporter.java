package be.sysa.quartz.initializer.service;

import be.sysa.quartz.CronDescription;
import be.sysa.quartz.initializer.api.GroupDefinitionApi;
import be.sysa.quartz.initializer.api.JobDefinitionApi;
import be.sysa.quartz.initializer.api.ScheduleDefinitionApi;
import be.sysa.quartz.initializer.api.TriggerDefinitionApi;
import be.sysa.quartz.initializer.support.CronDescriptionService;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Value
@Slf4j
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class ScheduleExporter {

    ScheduleAccessor scheduleAccessor;

    public ScheduleExporter(Scheduler scheduler) {
        this.scheduleAccessor = new ScheduleAccessor(scheduler);
    }

    @SneakyThrows
    public void export(OutputStream outputStream) {
        writeHeader(outputStream);
        ScheduleLoader.writeStream(outputStream, readExistingSchedule());
    }

    @SneakyThrows
    public ScheduleDefinitionApi readExistingSchedule() {
        ScheduleDefinitionApi.ScheduleDefinitionApiBuilder scheduleBuilder = ScheduleDefinitionApi.builder();
        scheduleAccessor.getJobGroupNames().forEach(
                groupName -> scheduleBuilder.group(readGroup(groupName))
        );
        return scheduleBuilder.build();
    }

    private GroupDefinitionApi readGroup(String groupName) {
        GroupDefinitionApi.GroupDefinitionApiBuilder groupBuilder = GroupDefinitionApi.builder().name(groupName);
        scheduleAccessor.getJobsInGroup(groupName).forEach(
                jobKey -> groupBuilder.job(readJob(jobKey))
        );
        return groupBuilder.build();
    }

    private JobDefinitionApi readJob(JobKey jobKey) {
        JobDetail jobDetail = scheduleAccessor.getJobDetail(jobKey);
        JobDefinitionApi.JobDefinitionApiBuilder jobBuilder = JobDefinitionApi.builder()
                .name(jobKey.getName())
                .jobClass(jobDetail.getJobClass().getName())
                .recover(jobDetail.requestsRecovery())
                .durable(jobDetail.isDurable())
                .description(jobDetail.getDescription())
                .jobDataMap(jobDetail.getJobDataMap());
        List<? extends Trigger> triggers = scheduleAccessor.getTriggersOfJob(jobKey);
        for (Trigger trigger : triggers) {
            if (trigger instanceof CronTrigger) {
                jobBuilder.trigger(readCronTrigger(trigger));
            }
        }
        return jobBuilder.build();
    }

    private TriggerDefinitionApi readCronTrigger(Trigger trigger) {
        CronDescription cronDescription = CronDescriptionService.instance();

        CronTrigger cronTrigger = (CronTrigger) trigger;
        TriggerKey triggerKey = trigger.getKey();
        String cronExpression = cronTrigger.getCronExpression();
        return TriggerDefinitionApi.builder()
                .name(cronTrigger.getKey().getName())
                .timeZone(cronTrigger.getTimeZone().getID())
                .cronExpression(cronExpression)
                .description(cronDescription.getDescription(cronExpression))
                .priority(getPriority(cronTrigger))
                .misfireExecution(hasMisfireExecution(cronTrigger))
                .triggerGroup(getTriggerGroup(trigger.getJobKey(), triggerKey))
                .jobDataMap(cronTrigger.getJobDataMap())
                .build();
    }

    private static final String HEADER_TEMPLATE =
            "#======================================================================\n" +
                    "# Schedule exported from scheduler: '%s' at %s \n" +
                    "# When a job has multiple triggers it may be possible to combine them.\n" +
                    "# Also check the trigger names are what you expect.\n" +
                    "#======================================================================\n";

    @SneakyThrows
    private void writeHeader(OutputStream outputStream) {
        PrintWriter printWriter = new PrintWriter(outputStream);
        printWriter.printf(HEADER_TEMPLATE, scheduleAccessor.getSchedulerName(), Instant.now().truncatedTo(ChronoUnit.SECONDS).toString());
        printWriter.flush();
    }

    private boolean hasMisfireExecution(CronTrigger cronTrigger) {
        return cronTrigger.getMisfireInstruction() == CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW;
    }

    private static Integer getPriority(CronTrigger cronTrigger) {
        int priority = cronTrigger.getPriority();
        return priority == 0 ? null : priority;
    }

    private static String getTriggerGroup(JobKey jobKey, TriggerKey triggerKey) {
        String triggerGroup = triggerKey.getGroup();
        return Objects.equals(triggerGroup, jobKey.getGroup()) ? null : triggerGroup;
    }
}
