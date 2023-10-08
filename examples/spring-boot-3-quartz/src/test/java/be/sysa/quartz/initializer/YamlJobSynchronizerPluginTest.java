package be.sysa.quartz.initializer;

import be.sysa.quartz.initializer.fixtures.*;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.*;
import org.quartz.spi.ClassLoadHelper;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class YamlJobSynchronizerPluginTest extends IntegrationTest {
    public static final String FILE_NAMES = "file1";
    public static final String JOB_NAME = "xxx";
    public static final String GROUP_NAME = "MyGroup";
    public static final String TRIGGER_NAME = "My Trigger";
    public static final String GENERATED_CRON_DESCRIPTION = "Every 5 minutes, starting at 1 minutes past the hour, between 06:00 and 20:59, only on Monday, Tuesday, Wednesday, Thursday, and Friday";

    @Autowired
    private Scheduler scheduler;
    @Mock
    private ClassLoadHelper classLoadHelper;

    YamlJobSynchronizerPlugin plugin = new YamlJobSynchronizerPlugin();

    @SneakyThrows
    @Test
    public void initializePlugin() {

        ScheduleTemplate schedule = ScheduleTemplate.builder()
                .group1(group(job(jobData(), trigger(triggerData()))))
                .build();

        plugin.setFileNames(FILE_NAMES);
        plugin.initialize("YamlPlugin", scheduler, classLoadHelper);
        when(classLoadHelper.getResourceAsStream(FILE_NAMES)).thenReturn(schedule.inputStream());
        plugin.start();
        JobDetail jobDetail = scheduler.getJobDetail(JobKey.jobKey(JOB_NAME, GROUP_NAME));
        assertThat(jobDetail.getJobClass().getName()).isEqualTo(JobTemplate.DEFAULT_CLASS);
        Trigger trigger = scheduler.getTrigger(TriggerKey.triggerKey(TRIGGER_NAME, GROUP_NAME));
        assertThat(trigger.getDescription()).isEqualTo(GENERATED_CRON_DESCRIPTION);

    }

    private static JobDataMapTemplate triggerData() {
        return JobDataMapTemplate.forTrigger()
                .key1("trigger-key1").value1("value1")
                .build();
    }

    private static JobDataMapTemplate jobData() {
        JobDataMapTemplate jobDataMap = JobDataMapTemplate.forJob()
                .key1("job-key1").value1("y1")
                .build();
        return jobDataMap;
    }

    private static TriggerTemplate trigger(JobDataMapTemplate triggerJobDataMap) {
        return TriggerTemplate.builder()
                .triggerName(TRIGGER_NAME)
                .cron1("0 1/5 6-20 ? * MON,TUE,WED,THU,FRI *")
                .jobDataMap(triggerJobDataMap)
                .build();
    }

    private static GroupTemplate group(JobTemplate job1) {
        return GroupTemplate.builder()
                .groupName(GROUP_NAME)
                .job1(job1)
                .build();
    }

    private static JobTemplate job(JobDataMapTemplate jobDataMap, TriggerTemplate trigger1) {
        return JobTemplate.builder()
                .jobName(JOB_NAME)
                .jobDataMap(jobDataMap)
                .trigger1(trigger1).build();
    }
}
