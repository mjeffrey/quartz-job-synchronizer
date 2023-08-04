package be.sysa.quartz.jobs;

import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotAJob {

    public void execute(JobExecutionContext context) {
        log.info("Job ** {} ** fired @ {}", context.getJobDetail().getKey().getName(), context.getFireTime());
        log.info("Next job scheduled @ {}", context.getNextFireTime());
    }
}
