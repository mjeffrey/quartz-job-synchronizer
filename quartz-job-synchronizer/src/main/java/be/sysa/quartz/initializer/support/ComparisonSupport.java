package be.sysa.quartz.initializer.support;

import be.sysa.quartz.initializer.model.JobDefinition;
import be.sysa.quartz.initializer.model.TriggerDefinition;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Trigger;

/**
 * This class provides comparison support for checking if certain details of a job or trigger
 * have changed compared to their definitions.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ComparisonSupport {

    /**
     * Checks if the given job definition has changed compared to the job detail.
     * It compares the properties of the job detail with the corresponding properties
     * of the job definition using a Differencer object.
     *
     * @param jobDetail     the current job detail object
     * @param jobDefinition the new job definition object
     * @return true if there are differences between the job detail and the job definition,
     *         false otherwise
     */
    public static boolean jobDefinitionChanged(JobDetail jobDetail, JobDefinition jobDefinition) {
        Differencer difference = Differencer.difference(jobDetail, jobDefinition);
        return difference.logDifferences().size() > 0;
    }

    /**
     * Checks if the given trigger has changed compared to the trigger definition.
     * If the trigger is an instance of CronTrigger, it will check for changes using the
     * cronTriggerChanged() method. Otherwise, it will return true, indicating that
     * the trigger has changed.
     *
     * @param trigger           the current trigger object
     * @param triggerDefinition the new trigger definition object
     * @return true if there are differences between the trigger and the trigger definition,
     *         false otherwise
     */
    public static boolean triggerChanged(Trigger trigger, TriggerDefinition triggerDefinition) {
        if (trigger instanceof CronTrigger) {
            return cronTriggerChanged((CronTrigger) trigger, triggerDefinition);
        } else {
            return true;
        }
    }

    /**
     * Checks if the given CronTrigger has changed compared to the trigger definition.
     *
     * @param trigger           the current CronTrigger object
     * @param triggerDefinition the new trigger definition object
     * @return true if there are differences between the trigger and the trigger definition,
     *         false otherwise
     */
    private static boolean cronTriggerChanged(CronTrigger trigger, TriggerDefinition triggerDefinition) {
        Differencer difference = Differencer.difference(trigger, triggerDefinition);
        return difference.logDifferences().size() > 0;
    }

}
