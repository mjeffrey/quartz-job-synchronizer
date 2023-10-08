package be.sysa.quartz.initializer.service;

import be.sysa.quartz.initializer.model.DependencyDefinition;
import be.sysa.quartz.initializer.model.ZonedTime;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * A {@link JobListener} that triggers a dependent job after the parent job has executed successfully.
 * The dependent job is defined by a {@link DependencyDefinition}.
 */
@Value
@Slf4j
public class DependentJobListener implements JobListener {
    DependencyDefinition dependency;

    private DependentJobListener(DependencyDefinition dependency) {
        this.dependency = dependency;
    }

    /**
     * Creates a new instance of DependentJobListener with the given dependency definition.
     *
     * @param dependency the dependency definition of the job listener
     * @return a new instance of DependentJobListener
     */
    public static DependentJobListener create(DependencyDefinition dependency) {
        return new DependentJobListener(dependency);
    }

    /**
     * Returns the name of the DependentJobListener (the name of the dependency)
     *
     * @return the name of the DependentJobListener
     */
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
        if (abortOnParentException(jobException, parentJobKey, childJobKey)) {
            return;
        }
        Scheduler scheduler = context.getScheduler();
        StartTime startTime = new StartTime(dependency, context.getFireTime().toInstant(), Instant.now());
        Trigger trigger = TriggerBuilder.newTrigger().forJob(childJobKey)
                .usingJobData(new JobDataMap(dependency.getJobDataMap()))
                .withIdentity(dependency.getChildJob().getGroup())
                .withPriority(dependency.getPriority())
                .startAt(Date.from(startTime.getStart()))
                .build();
        log.debug("Triggering job {} as a result of {} startTime: {}",
                childJobKey,
                parentJobKey,
                startTime
        );
        scheduler.scheduleJob(trigger);
    }

    private boolean abortOnParentException(JobExecutionException jobException, JobKey parentJobKey, JobKey childJobKey) {
        if (jobException != null) {
            Throwable cause = jobException.getCause();
            if (dependency.isParentErrorIgnored()) {
                log.info("Ignoring exception in parent job {}: {}",
                        parentJobKey,
                        cause == null ? jobException.getMessage() : cause.getMessage());
            } else {
                log.error("Not scheduling job: {} due to exception in parent job: {} ",
                        childJobKey,
                        parentJobKey,
                        cause);
                return true;
            }
        }
        return false;
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        log.debug("Job Vetoed {}", context.getJobDetail().getKey());
    }

    static class StartTime {
        @Getter
        private Instant start;
        @Getter
        private boolean isImmediate;

        @Override
        public String toString() {
            return isImmediate ? "IMMEDIATE" : start.toString();
        }

        public StartTime(DependencyDefinition dependency, Instant parentFireTime, Instant now) {
            ZonedTime notBefore = dependency.getNotBefore();
            start = now;
            isImmediate = true;
            if (notBefore != null) {
                LocalDate localDate = LocalDate.ofInstant(parentFireTime, notBefore.getZone());
                Instant notBeforeInstant = ZonedDateTime.of(localDate, notBefore.getLocalTime(), notBefore.getZone()).toInstant();
                if (now.isBefore(notBeforeInstant)) {
                    start = notBeforeInstant;
                    isImmediate = false;
                }
            } else {
                int secondsDelay = dependency.getSecondsDelay();
                if (secondsDelay > 0) {
                    start = now.plusSeconds(secondsDelay);
                    isImmediate = false;
                }
            }
        }
    }

}
