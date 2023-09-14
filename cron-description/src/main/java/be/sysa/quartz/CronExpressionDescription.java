package be.sysa.quartz;

import it.burning.cron.CronExpressionDescriptor;
import lombok.extern.slf4j.Slf4j;

import static it.burning.cron.CronExpressionParser.Options;

/**
 * A class that generates human-readable descriptions for cron expressions.
 */
@Slf4j
public class CronExpressionDescription implements CronDescription{

    /**
     * Returns a human-readable description of the given cron expression.
     * Uses CronExpressionDescriptor to generate the description.
     *
     * @param cron The cron expression string.
     * @return The human-readable description of the cron expression.
     *         Returns null if there is no cron expression parser in the path.
     */
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
