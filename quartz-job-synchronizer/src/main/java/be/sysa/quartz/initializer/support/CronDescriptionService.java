package be.sysa.quartz.initializer.support;

import be.sysa.quartz.CronDescription;

import java.util.ServiceLoader;

public class CronDescriptionService {
    public static CronDescription instance() {
        return ServiceLoader.load(CronDescription.class).findFirst().orElseGet(() -> cron -> null);
    }
}
