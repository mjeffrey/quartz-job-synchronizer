package be.sysa.quartz;

/**
 * The CronDescription interface represents the contract for classes that provide a description for a given cron expression.
 */
public interface CronDescription {
    /**
     * Returns the description of the given cron expression.
     *
     * @param cron The cron expression to generate the description for.
     * @return The description of the cron expression.
     */
    String getDescription(String cron);
}
