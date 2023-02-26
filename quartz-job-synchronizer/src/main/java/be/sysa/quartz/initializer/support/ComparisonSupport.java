package be.sysa.quartz.initializer.support;

import be.sysa.quartz.initializer.model.JobDefinition;
import be.sysa.quartz.initializer.model.TriggerDefinition;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Trigger;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ComparisonSupport {

    public static boolean jobDefinitionChanged(JobDetail jobDetail, JobDefinition jobDefinition) {
        Differencer difference = Differencer.difference(jobDetail, jobDefinition);
        return difference.logDifferences().size() > 0;
    }

    public static boolean triggerChanged(Trigger trigger, TriggerDefinition triggerDefinition) {
        if (trigger instanceof CronTrigger) {
            return cronTriggerChanged((CronTrigger) trigger, triggerDefinition);
        } else {
            return true;
        }
    }

    private static boolean cronTriggerChanged(CronTrigger trigger, TriggerDefinition triggerDefinition) {
        Differencer difference = Differencer.difference(trigger, triggerDefinition);
        return difference.logDifferences().size() > 0;
    }

}
