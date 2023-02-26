package be.sysa.quartz;

import it.burning.cron.CronExpressionDescriptor;
import lombok.extern.slf4j.Slf4j;

import static it.burning.cron.CronExpressionParser.Options;

@Slf4j
public class CronExpressionDescription implements CronDescription{

    @Override
    public String getDescription(String cron) {
        try {
            Options options = new Options(){{
                setUse24HourTimeFormat(true);
            }};
            return CronExpressionDescriptor.getDescription(cron, options);
        } catch (Exception e) {
            log.warn("No cron expression parser in the path.");
            return null;
        }
    }
}
