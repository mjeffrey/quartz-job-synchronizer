package be.sysa.quartz.initializer.service;

import be.sysa.quartz.initializer.model.DependencyDefinition;
import be.sysa.quartz.initializer.model.ZonedTime;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;

@Value
@Slf4j
public class DependentJobListener implements JobListener {
    DependencyDefinition dependency;

    private DependentJobListener(DependencyDefinition dependency) {
        this.dependency = dependency;
    }

    public static DependentJobListener create(DependencyDefinition dependency) {
        return new DependentJobListener(dependency);
    }

    @Override
    public String getName() {
        return dependency.getName();
    }

    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
    }

    @SneakyThrows
    @Override
    public void jobWasExecuted(JobExecutionContext context,
                               JobExecutionException jobException) {
        JobKey parentJobKey = context.getJobDetail().getKey();
        JobKey childJobKey = dependency.getChildJob();
        if (jobException != null) {
            if (dependency.isParentErrorIgnored()) {
                log.info("Ignoring exception in parent job {}: {}",
                        parentJobKey,
                        jobException.getCause().getMessage());
            } else {
                log.error("Not scheduling job: {} due to exception in parent job: {} ",
                        childJobKey,
                        parentJobKey,
                        jobException.getCause());
                return;
            }
        }
        Scheduler scheduler = context.getScheduler();
        StartTime startTime = new StartTime(context, dependency);
        Trigger trigger = TriggerBuilder.newTrigger().forJob(childJobKey)
                .usingJobData(new JobDataMap(dependency.getJobDataMap()))
                .withPriority(dependency.getPriority())
                .startAt(startTime.getStart())
                .build();
        log.debug("Triggering job {} as a result of {} startTime: {}",
                childJobKey,
                parentJobKey,
                startTime
        );
        scheduler.scheduleJob(trigger);
    }

    private static class StartTime {
        private Instant start;
        private boolean isImmediate;

        public Date getStart() {
            return Date.from(start);
        }

        @Override
        public String toString() {
            return isImmediate ? "IMMEDIATE" : start.toString();
        }

        public StartTime(JobExecutionContext context, DependencyDefinition dependency) {
            ZonedTime notBefore = dependency.getNotBefore();
            final Instant now = Instant.now();
            start = now;
            isImmediate = true;
            if (notBefore != null) {
                Instant fireTime = context.getFireTime().toInstant();
                LocalDate localDate = LocalDate.ofInstant(fireTime, notBefore.getZoneId());
                Instant notBeforeInstant = ZonedDateTime.of(localDate, notBefore.getLocalTime(), notBefore.getZoneId()).toInstant();
                if (now.isBefore(notBeforeInstant)) {
                    start = notBeforeInstant;
                    isImmediate = false;
                }
            }else{
                int secondsDelay = dependency.getSecondsDelay();
                if ( secondsDelay > 0 ){
                    start = now.plusSeconds(dependency.getSecondsDelay());
                    isImmediate = false;
                }
            }
        }
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        // do something with the event
    }
}
