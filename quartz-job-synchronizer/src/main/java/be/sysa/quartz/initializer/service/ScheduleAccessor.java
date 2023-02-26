package be.sysa.quartz.initializer.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;

import java.util.List;
import java.util.Set;

@Value
@Slf4j
@AllArgsConstructor
public class ScheduleAccessor {
    Scheduler scheduler;

    // ====================== Get jobs/triggers ======================
    @SneakyThrows
    List<String> getJobGroupNames() {
        return scheduler.getJobGroupNames();
    }

    @SneakyThrows
    Set<JobKey> getJobsInGroup(String groupName) {
        return scheduler.getJobKeys(GroupMatcher.groupEquals(groupName));
    }

    @SneakyThrows
    JobDetail getJobDetail(JobKey jobToUpdate) {
        return scheduler.getJobDetail(jobToUpdate);
    }

    @SneakyThrows
    List<? extends Trigger> getTriggersOfJob(@NonNull JobKey jobKey) {
        return scheduler.getTriggersOfJob(jobKey);
    }

    @SneakyThrows
    Trigger getTrigger(TriggerKey triggerKey) {
        return scheduler.getTrigger(triggerKey);
    }


    // ====================== Add jobs/triggers ======================
    @SneakyThrows
    void addJob(JobDetail jobDetail) {
        scheduler.addJob(jobDetail, true, true);
    }

    @SneakyThrows
    void rescheduleJob(TriggerKey triggerKey, CronTrigger cronTrigger) {
        log.info("rescheduling trigger {}", triggerKey);
        scheduler.rescheduleJob(triggerKey, cronTrigger);
    }

    @SneakyThrows
    public void scheduleJob(Trigger trigger) {
        log.info("Scheduling trigger {} {}",
                trigger.getKey(),
                trigger.getDescription());
        scheduler.scheduleJob(trigger);
    }


    // ====================== Delete jobs/triggers ======================
    void deleteJobsInGroup(String group) {
        getJobsInGroup(group).forEach(this::deleteJob);
    }

    @SneakyThrows
    void deleteJob(JobKey jobKey) {
        log.info("Deleting job (and triggers) {}", jobKey);
        scheduler.deleteJob(jobKey);
    }

    @SneakyThrows
    void deleteTrigger(TriggerKey triggerKey) {
        log.info("Deleting trigger {}", triggerKey);
        scheduler.unscheduleJob(triggerKey);
    }

    @SneakyThrows
    public String getSchedulerName() {
        return scheduler.getSchedulerName();
    }
}
