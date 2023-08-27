package be.sysa.quartz.initializer.service;

import be.sysa.quartz.initializer.model.DependencyDefinition;
import be.sysa.quartz.initializer.model.ZonedTime;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.Mock.Strictness;
import org.mockito.invocation.Invocation;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.*;

import java.time.Instant;
import java.util.Collection;

import static be.sysa.quartz.initializer.service.DependentJobListener.StartTime;
import static be.sysa.quartz.initializer.service.DependentJobListener.create;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DependentJobListenerTest {
    private static final JobKey PARENT_JOB = JobKey.jobKey("parent", "group1");
    private static final JobKey CHILD_JOB = JobKey.jobKey("child", "group2");
    private static final String DEPENDENCY_NAME = "name";
    private final DependencyDefinition.DependencyDefinitionBuilder DEPENDENCY_BUILDER = DependencyDefinition.builder()
            .name(DEPENDENCY_NAME)
            .childJob(CHILD_JOB);
    DependentJobListener dependentJobListener;
    @Mock(strictness = Strictness.LENIENT, answer = Answers.RETURNS_DEEP_STUBS)
    JobExecutionContext context;

    @Mock
    Scheduler scheduler;

    @Captor
    ArgumentCaptor<Trigger> triggerArgumentCaptor;

    @BeforeEach
    void setUp() {
        when(context.getScheduler()).thenReturn(scheduler);
        when(context.getJobDetail().getKey()).thenReturn(PARENT_JOB);
    }

    @SneakyThrows
    @DisplayName("Once parent is executed, Child is triggered ")
    @Test
    public void childJobExecuted() {
        Trigger trigger = withDependency(DEPENDENCY_BUILDER.build()).execute();
        assertThat(trigger.getJobKey()).isEqualTo(CHILD_JOB);
        assertThat(trigger.getKey().getName()).isEqualTo(CHILD_JOB.getGroup());
        assertThat(dependentJobListener.getName()).isEqualTo(DEPENDENCY_NAME);
    }

    @Test
    @DisplayName("Job Vetoed does nothing")
    public void jobVetoedDoesNothing() {
        withDependency(DEPENDENCY_BUILDER.build());
        assertThatNoException().isThrownBy(()->dependentJobListener.jobExecutionVetoed(context));
    }

    @Test
    @DisplayName("By default if the parent fails with error, the child job is not executed")
    public void childJobNotExecutedDueToExceptionInParent() {
        Trigger trigger = withDependency(DEPENDENCY_BUILDER.build()).execute(new JobExecutionException());
        assertThat(trigger).isNull();
    }

    @Test
    @DisplayName("When configured, if the parent fails with error, the child job is executed")
    public void childJobExecutedEvenIfExceptionInParent() {
        Trigger trigger = withDependency(
                DEPENDENCY_BUILDER.parentErrorIgnored(true)
                        .build())
                .execute(new JobExecutionException());
        assertThat(trigger).isNotNull();
    }


    @Test
    @DisplayName("With normal execution we start immediately (now)")
    public void immediateStart() {
        Instant now = Instant.now();
        Instant parentFireTime = now.minusSeconds(10);
        StartTime startTime = new StartTime(DEPENDENCY_BUILDER.build(), parentFireTime, now);
        assertThat(startTime)
                .returns(true, StartTime::isImmediate)
                .returns(now, StartTime::getStart)
        ;
    }

    @Test
    @DisplayName("With delayed execution we start now + X seconds")
    public void delayedStart() {
        Instant now = Instant.now();
        Instant parentFireTime = now.minusSeconds(10);

        final int secondsDelay = 65;
        StartTime startTime = new StartTime(DEPENDENCY_BUILDER.secondsDelay(secondsDelay).build(), parentFireTime, now);
        assertThat(startTime)
                .returns(now.plusSeconds(secondsDelay), StartTime::getStart)
                .returns(false, StartTime::isImmediate);
    }

    @Test
    @DisplayName("With startAfter and parent executed, start later")
    public void delayedStartAfterTime() {
        Instant now = Instant.parse("2023-03-10T15:00:00Z");
        Instant parentFireTime = now.minusSeconds(10);

        StartTime startTime = new StartTime(DEPENDENCY_BUILDER
                .notBefore(ZonedTime.parse("16:21:00Z"))
                .build(), parentFireTime, now);
        assertThat(startTime)
                .returns(Instant.parse("2023-03-10T16:21:00Z"), StartTime::getStart)
                .returns(false, StartTime::isImmediate);
    }

    @Test
    @DisplayName("With startAfter and parent executed in a Timezone, start later taking into account the timezone")
    public void delayedStartAfterTimeWithTZ() {
        Instant now = Instant.parse("2023-03-10T15:00:00Z");
        Instant parentFireTime = now.minusSeconds(10);

        StartTime startTime = new StartTime(DEPENDENCY_BUILDER
                .notBefore(ZonedTime.parse("16:21:00 Europe/Brussels")) // 1 Hour ahead of UTC
                .build(), parentFireTime, now);
        assertThat(startTime)
                .returns(Instant.parse("2023-03-10T15:21:00Z"), StartTime::getStart)
                .returns(false, StartTime::isImmediate);
    }

    @Test
    @DisplayName("With startAfter and parent executed later, start immediately")
    public void parentExecutedAfterStartAfter() {
        Instant now = Instant.parse("2023-03-10T15:00:00Z");
        Instant parentFireTime = now.minusSeconds(10);

        StartTime startTime = new StartTime(DEPENDENCY_BUILDER
                .notBefore(ZonedTime.parse("02:00 Europe/Brussels"))
                .build(), parentFireTime, now);
        assertThat(startTime)
                .returns(now, StartTime::getStart)
                .returns(true, StartTime::isImmediate);
    }


    @SneakyThrows
    private Trigger execute() {
        return execute(null);
    }

    @SneakyThrows
    private Trigger execute(JobExecutionException jobException) {
        dependentJobListener.jobWasExecuted(context, jobException);
        return getFiredTrigger();
    }

    private DependentJobListenerTest withDependency(DependencyDefinition dependencyDefinition) {
        dependentJobListener = create(dependencyDefinition);
        return this;
    }

    private Trigger getFiredTrigger() throws SchedulerException {
        Collection<Invocation> invocations = Mockito.mockingDetails(scheduler).getInvocations();
        int numberOfCalls = invocations.size();
        if (numberOfCalls == 0) {
            return null;
        }
        verify(scheduler).scheduleJob(triggerArgumentCaptor.capture());
        return triggerArgumentCaptor.getValue();
    }
}
