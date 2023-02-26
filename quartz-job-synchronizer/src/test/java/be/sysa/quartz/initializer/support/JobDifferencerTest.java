package be.sysa.quartz.initializer.support;

import be.sysa.quartz.initializer.fixtures.jobs.MyTestJob;
import be.sysa.quartz.initializer.fixtures.jobs.MyTestJob2;
import be.sysa.quartz.initializer.model.JobDefinition;
import be.sysa.quartz.initializer.model.TriggerDefinition;
import org.junit.jupiter.api.Test;
import org.quartz.JobBuilder;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.quartz.JobBuilder.newJob;

public class JobDifferencerTest {
    private static final String GROUP_NAME = "MyGroup";
    private static final String JOB_NAME = "MyJob";
    private static final String DESCRIPTION = "description";
    private final JobKey jobKey = JobKey.jobKey(JOB_NAME, GROUP_NAME);

    JobBuilder existingJob = newJob(MyTestJob.class)
            .withIdentity(jobKey)
            .storeDurably(false)
            .requestRecovery(false)
            .withDescription(DESCRIPTION)
            ;

    TriggerDefinition triggerDefinition = TriggerDefinition.builder()
            .triggerKey(TriggerKey.triggerKey("triggerName", "triggerGroup"))
            .build();
    JobDefinition newJob = JobDefinition.builder()
            .jobKey(jobKey)
            .durable(false)
            .recover(false)
            .jobClass(MyTestJob.class)
            .description(DESCRIPTION)
            .triggers(Map.of(triggerDefinition.getTriggerKey(), triggerDefinition))
            .build();

    @Test
    public void jobUnchanged() {
        assertThat(Differencer.difference(existingJob.build(), newJob).logDifferences()).hasSize(0);
    }


    @Test
    public void jobDataMapDeleted() {
        existingJob.usingJobData("datakey1", "valueX");
        JobDefinition anotherGroup = newJob.toBuilder().jobDataMap(Collections.emptyMap()).build();
        assertDifference(anotherGroup, Difference.Type.DATA_MAP_DELETED);
    }

    @Test
    public void durableChanged() {
        JobDefinition anotherJob = newJob.toBuilder().durable(true).build();
        assertDifference(anotherJob, Difference.Type.DURABLE);
    }

    @Test
    public void recoverChanged() {
        JobDefinition anotherJob = newJob.toBuilder().recover(true).build();
        assertDifference(anotherJob, Difference.Type.RECOVERY);
    }

    @Test
    public void jobClassChanged() {
        JobDefinition anotherJob = newJob.toBuilder().jobClass(MyTestJob2.class).build();
        assertDifference(anotherJob, Difference.Type.JOB_CLASS);
    }

    @Test
    public void jobDataMapRemovedElement() {
        existingJob.usingJobData("datakey1", "valueX");
        existingJob.usingJobData("datakey2", 2.53);
        JobDefinition anotherGroup = newJob.toBuilder().jobDataMap(Map.of("x", "y")).build();
        assertDifference(anotherGroup, Difference.Type.DATA_MAP_ENTRIES_CHANGE);
    }

    @Test
    public void jobDataMapAddNew() {
        JobDefinition anotherGroup = newJob.toBuilder().jobDataMap(Map.of("x", "y")).build();
        assertDifference(anotherGroup, Difference.Type.DATA_MAP_NEW);
    }

    @Test
    public void jobDataChangeValue() {
        existingJob.usingJobData("datakey1", "valueX");
        JobDefinition anotherGroup = newJob.toBuilder().jobDataMap(Map.of("datakey1", 1000)).build();
        assertDifference(anotherGroup, Difference.Type.DATA_MAP_ENTRY_VALUE_CHANGED);
    }

    private void assertDifference(JobDefinition anotherGroup, Difference.Type type) {
        assertThat(Differencer.difference(existingJob.build(), anotherGroup).logDifferences()).hasSize(1)
                .extracting(Difference::getType).contains(type);
    }

}
