package be.sysa.quartz.jobs;

import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@DisallowConcurrentExecution
public class SampleJob implements Job {
    public void execute(JobExecutionContext context) {
        log.info("Job ** {} ** fired @ {} Next job scheduled @ {}",
                context.getJobDetail().getKey().getName(),
                context.getFireTime(),
                context.getNextFireTime());
    }
}
