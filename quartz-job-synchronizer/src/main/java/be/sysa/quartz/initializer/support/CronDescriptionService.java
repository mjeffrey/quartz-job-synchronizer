package be.sysa.quartz.initializer.support;

import be.sysa.quartz.CronDescription;

import java.util.ServiceLoader;

/**
 * A service for retrieving an instance of {@link CronDescription}.
 *
 * This class provides a convenient way to retrieve an instance of {@link CronDescription} through the use of the
 * {@link ServiceLoader} mechanism.
 */
public class CronDescriptionService {
    /**
     * Returns an instance of the CronDescription interface.
     *
     * @return the singleton instance of CronDescription, or null if no implementation is found
     */
    public static CronDescription instance() {
        return ServiceLoader.load(CronDescription.class).findFirst().orElseGet(() -> cron -> null);
    }
}
