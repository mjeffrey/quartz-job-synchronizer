package be.sysa.quartz.initializer.service;

import be.sysa.quartz.initializer.model.DependencyDefinition;
import lombok.SneakyThrows;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

import java.time.Instant;
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
        Instant startTime = Instant.now().plusSeconds(dependency.getSecondsDelay());
        Trigger trigger = TriggerBuilder.newTrigger().forJob(childJobKey)
                .usingJobData(new JobDataMap(dependency.getJobDataMap()))
                .withPriority(dependency.getPriority())
                .startAt(Date.from(startTime))
                .build();
        String startString = dependency.getSecondsDelay() == 0 ? "IMMEDIATE" : startTime.toString();
        log.debug("Triggering job {} as a result of {} startTime: {}",
                childJobKey,
                parentJobKey,
                startString
        );
        scheduler.scheduleJob(trigger);
    }

    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        // do something with the event
    }
}
