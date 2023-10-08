package be.sysa.quartz.initializer.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.*;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ScheduleAccessorTest {
    @InjectMocks
    ScheduleAccessor scheduleAccessor;
    @Mock
    Scheduler scheduler;

    @Mock
    JobDetail jobDetail;

    @Mock
    CronTrigger cronTrigger;

    private static final String GROUP_NAME = "group";
    private static final JobKey JOB_KEY = JobKey.jobKey("jobKey");
    private static final TriggerKey TRIGGER_KEY = TriggerKey.triggerKey("triggerKey");

    @Test
    public void getJobGroupNames() throws SchedulerException {
        scheduleAccessor.getJobGroupNames();
        verify(scheduler, times(1)).getJobGroupNames();
        when(scheduler.getJobGroupNames()).thenThrow(IllegalArgumentException.class);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> scheduleAccessor.getJobGroupNames());
    }

    @Test
    public void getJobsInGroup() throws SchedulerException {
        scheduleAccessor.getJobsInGroup(GROUP_NAME);
        verify(scheduler, times(1)).getJobKeys(any());
        when(scheduler.getJobKeys(any())).thenThrow(IllegalArgumentException.class);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> scheduleAccessor.getJobsInGroup(GROUP_NAME));
    }

    @Test
    public void getJobDetail() throws SchedulerException {
        scheduleAccessor.getJobDetail(JOB_KEY);
        verify(scheduler, times(1)).getJobDetail(JOB_KEY);
        when(scheduler.getJobDetail(JOB_KEY)).thenThrow(IllegalArgumentException.class);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> scheduleAccessor.getJobDetail(JOB_KEY));
    }

    @Test
    public void getTriggersOfJob() throws SchedulerException {
        scheduleAccessor.getTriggersOfJob(JOB_KEY);
        verify(scheduler, times(1)).getTriggersOfJob(JOB_KEY);
        when(scheduler.getTriggersOfJob(JOB_KEY)).thenThrow(IllegalArgumentException.class);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> scheduleAccessor.getTriggersOfJob(JOB_KEY));
    }

    @Test
    public void getTrigger() throws SchedulerException {
        scheduleAccessor.getTrigger(TRIGGER_KEY);
        verify(scheduler, times(1)).getTrigger(TRIGGER_KEY);
        when(scheduler.getTrigger(TRIGGER_KEY)).thenThrow(IllegalArgumentException.class);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> scheduleAccessor.getTrigger(TRIGGER_KEY));
    }

    @Test
    public void getTriggerState() throws SchedulerException {
        scheduleAccessor.getTriggerState(TRIGGER_KEY);
        verify(scheduler, times(1)).getTriggerState(TRIGGER_KEY);
        when(scheduler.getTriggerState(TRIGGER_KEY)).thenThrow(IllegalArgumentException.class);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> scheduleAccessor.getTriggerState(TRIGGER_KEY));
    }

    @Test
    public void addJob() throws SchedulerException {
        scheduleAccessor.addJob(jobDetail);
        verify(scheduler, times(1)).addJob(jobDetail, true, true);
        doThrow(new IllegalArgumentException()).when(scheduler).addJob(jobDetail, true, true);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> scheduleAccessor.addJob(jobDetail));
    }

    @Test
    public void rescheduleJob() throws SchedulerException {
        scheduleAccessor.rescheduleJob(TRIGGER_KEY, cronTrigger);
        verify(scheduler, times(1)).rescheduleJob(TRIGGER_KEY, cronTrigger);
        doThrow(new IllegalArgumentException()).when(scheduler).rescheduleJob(TRIGGER_KEY, cronTrigger);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> scheduleAccessor.rescheduleJob(TRIGGER_KEY, cronTrigger));
    }

    @Test
    public void scheduleJob() throws SchedulerException {
        scheduleAccessor.scheduleJob(cronTrigger);
        verify(scheduler, times(1)).scheduleJob(cronTrigger);
        doThrow(new IllegalArgumentException()).when(scheduler).scheduleJob(cronTrigger);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> scheduleAccessor.scheduleJob(cronTrigger));
    }

    @Test
    public void deleteJobsInGroup() throws SchedulerException {
        when(scheduler.getJobKeys(any())).thenReturn(Set.of(JOB_KEY));
        scheduleAccessor.deleteJobsInGroup(GROUP_NAME);
        verify(scheduler, times(1)).deleteJob(JOB_KEY);

        doThrow(new IllegalArgumentException()).when(scheduler).deleteJob(any());
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> scheduleAccessor.deleteJobsInGroup(GROUP_NAME));
    }

    @Test
    public void deleteJob() throws SchedulerException {
        scheduleAccessor.deleteJob(JOB_KEY);
        verify(scheduler, times(1)).deleteJob(JOB_KEY);
        doThrow(new IllegalArgumentException()).when(scheduler).deleteJob(JOB_KEY);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> scheduleAccessor.deleteJob(JOB_KEY));
    }

    @Test
    public void unscheduleJob() throws SchedulerException {
        scheduleAccessor.unscheduleJob(TRIGGER_KEY);
        verify(scheduler, times(1)).unscheduleJob(TRIGGER_KEY);
        doThrow(new IllegalArgumentException()).when(scheduler).unscheduleJob(TRIGGER_KEY);
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> scheduleAccessor.unscheduleJob(TRIGGER_KEY));
    }

    @Test
    public void getSchedulerName() throws SchedulerException {
        scheduleAccessor.getSchedulerName();
        verify(scheduler, times(1)).getSchedulerName();
        doThrow(new IllegalArgumentException()).when(scheduler).getSchedulerName();
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> scheduleAccessor.getSchedulerName());
    }

    @Test
    public void getListenerManager() throws SchedulerException {
        scheduleAccessor.getListenerManager();
        verify(scheduler, times(1)).getListenerManager();
        doThrow(new IllegalArgumentException()).when(scheduler).getListenerManager();
        assertThatExceptionOfType(IllegalArgumentException.class).isThrownBy(() -> scheduleAccessor.getListenerManager());
    }

    @Test
    public void getScheduler() {
        Scheduler delegate = scheduleAccessor.getScheduler();
        assertThat(delegate).isSameAs(scheduler);
    }
}
