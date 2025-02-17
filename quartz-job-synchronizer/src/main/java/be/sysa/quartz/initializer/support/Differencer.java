package be.sysa.quartz.initializer.support;

import be.sysa.quartz.initializer.model.JobDefinition;
import be.sysa.quartz.initializer.model.TriggerDefinition;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.quartz.CronTrigger;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.quartz.utils.Key;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static be.sysa.quartz.initializer.support.Difference.Type.CRON;
import static be.sysa.quartz.initializer.support.Difference.Type.DATA_MAP_DELETED;
import static be.sysa.quartz.initializer.support.Difference.Type.DATA_MAP_ENTRIES_CHANGE;
import static be.sysa.quartz.initializer.support.Difference.Type.DATA_MAP_ENTRY_VALUE_CHANGED;
import static be.sysa.quartz.initializer.support.Difference.Type.DATA_MAP_NEW;
import static be.sysa.quartz.initializer.support.Difference.Type.DURABLE;
import static be.sysa.quartz.initializer.support.Difference.Type.JOB_CLASS;
import static be.sysa.quartz.initializer.support.Difference.Type.MISFIRE_EXECUTE_NOW;
import static be.sysa.quartz.initializer.support.Difference.Type.MISFIRE_IGNORE;
import static be.sysa.quartz.initializer.support.Difference.Type.PRIORITY;
import static be.sysa.quartz.initializer.support.Difference.Type.RECOVERY;
import static be.sysa.quartz.initializer.support.Difference.Type.TIMEZONE;
import static be.sysa.quartz.initializer.support.Difference.Type.TRIGGER_KEY;

@Value
@Slf4j
class Differencer {
    List<Difference> differences = new ArrayList<>();

    private Differencer(CronTrigger trigger, TriggerDefinition triggerDefinition) {
        TriggerKey key = trigger.getKey();
        changed(key, TRIGGER_KEY, key, triggerDefinition.getTriggerKey());
        changed(key, PRIORITY, trigger.getPriority(), triggerDefinition.getPriority());
        changed(key, CRON, trigger.getCronExpression(), triggerDefinition.getCronExpression());
        changed(key, TIMEZONE, trigger.getTimeZone(), triggerDefinition.getTimeZone());
        dataMapChanged(key, trigger.getJobDataMap(), triggerDefinition.getJobDataMap());
        misfireChange(trigger, triggerDefinition);
    }

    public Differencer(JobDetail jobDetail, JobDefinition jobDefinition) {
        JobKey key = jobDetail.getKey();
        changed(key, DURABLE, jobDetail.isDurable(), jobDefinition.isDurable());
        changed(key, RECOVERY, jobDetail.requestsRecovery(), jobDefinition.isRecover());
        changed(key, JOB_CLASS, jobDetail.getJobClass().getName(), jobDefinition.getJobClass().getName());
        dataMapChanged(key, jobDetail.getJobDataMap(), jobDefinition.getJobDataMap());
    }

    public static Differencer difference(CronTrigger trigger, TriggerDefinition triggerDefinition) {
        return new Differencer(trigger, triggerDefinition);
    }

    public static Differencer difference(JobDetail jobDetail, JobDefinition jobDefinition) {
        return new Differencer(jobDetail, jobDefinition);
    }

    public List<Difference> logDifferences() {
        differences.forEach(Difference::log);
        return differences;
    }

    void dataMapChanged(Key<?> key, JobDataMap existingDataMap, Map<String, Object> newDataMap) {
        if (isEmpty(newDataMap) && isEmpty(existingDataMap)) return;
        if (isEmpty(newDataMap)) {
            addJobDataMapDifference(key, DATA_MAP_DELETED);
        } else if (isEmpty(existingDataMap)) {
            addJobDataMapDifference(key, DATA_MAP_NEW);
        } else if (existingDataMap.size() != newDataMap.size()) {
            addJobDataMapDifference(key, DATA_MAP_ENTRIES_CHANGE, existingDataMap.size(), newDataMap.size());
        } else {
            for (Map.Entry<String, Object> existingEntry : existingDataMap.entrySet()) {
                Object newValue = newDataMap.get(existingEntry.getKey());
                Object existingValue = existingEntry.getValue();
                if (!Objects.equals(existingValue, newValue)) {
                    addJobDataMapDifference(key, DATA_MAP_ENTRY_VALUE_CHANGED, existingEntry, newValue);
                }
            }
        }
    }

    private static boolean isEmpty(Map<String, Object> newDataMap) {
        return newDataMap == null || newDataMap.isEmpty();
    }

    private void changed(Key<?> key, Difference.Type changeType, Object existingValue, Object newValue) {
        boolean hasChanged = !Objects.equals(existingValue, newValue);
        if (hasChanged) {
            differences.add(Difference.difference(key, changeType, existingValue, newValue));
        }
    }

    private void misfireChange(CronTrigger trigger, TriggerDefinition triggerDefinition) {
        TriggerKey triggerKey = trigger.getKey();
        boolean existingMisfire = isMisfireConfigured(trigger);
        boolean newMisfire = triggerDefinition.isMisfireExecution();
        if (existingMisfire == newMisfire) {
            return;
        }

        if (newMisfire) {
            differences.add(Difference.difference(triggerKey, MISFIRE_EXECUTE_NOW));
        } else {
            differences.add(Difference.difference(triggerKey, MISFIRE_IGNORE));
        }
    }

    private boolean isMisfireConfigured(CronTrigger trigger) {
        return trigger.getMisfireInstruction() == CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW;
    }

    private void addJobDataMapDifference(Key<?> key, Difference.Type type, Object existingValue, Object newValue) {
        differences.add(Difference.difference(key, type, existingValue, newValue));
    }

    private void addJobDataMapDifference(Key<?> key, Difference.Type type) {
        differences.add(Difference.difference(key, type));
    }
}
