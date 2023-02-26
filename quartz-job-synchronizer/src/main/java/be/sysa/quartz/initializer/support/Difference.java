package be.sysa.quartz.initializer.support;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.quartz.TriggerKey;
import org.quartz.utils.Key;

@Value
@Slf4j
class Difference {
    Key<?> key;
    Type type;
    Object existingValue;
    Object newValue;

    private Difference(Key<?> key, @NonNull Type type, Object existingValue, Object newValue) {
        this.key = key;
        this.type = type;
        this.existingValue = existingValue;
        this.newValue = newValue;
    }

    public static Difference difference(Key<?> key, @NonNull Type type, Object existingValue, Object newValue) {
        return new Difference(key, type, existingValue,newValue);
    }

    public static Difference difference(Key<?> key, @NonNull Type type) {
        return new Difference(key, type, null, null);
    }

    public void log(){
        log.info(formatMessage(key, type.getParameterizedString()), existingValue, newValue);
    }

    private static String formatMessage(Key<?> keyType, String message) {
        String prefix = keyType instanceof TriggerKey ? "Trigger" : "Job";
        return String.format("%s: '%s' %s", prefix, keyType.getName(), message);
    }

    @AllArgsConstructor
    enum Type {
        TRIGGER_KEY(change("triggerKey")),
        PRIORITY(change("priority")),
        CRON(change("cron")),
        TIMEZONE(change("timezone")),
        DURABLE(change("durable")),
        RECOVERY(change("recovery")),
        JOB_CLASS(change("job class")),
        DATA_MAP_DELETED("DataMap deleted"),
        DATA_MAP_NEW("DataMap added"),
        DATA_MAP_ENTRIES_CHANGE("The number of DataMap entries has changed"),
        DATA_MAP_ENTRY_VALUE_CHANGED("DataMap entry has a new value. entry: '{}' -> newValue: '{}'"),
        MISFIRE_EXECUTE_NOW("misfire changed. If the trigger misses its scheduled fire time it is now executed as soon as possible"),
        MISFIRE_IGNORE("misfire changed. If the trigger misses its scheduled fire time, it is IGNORED (and scheduled for the next time)"),
        ;

        @Getter
        private final String parameterizedString;

        private static String change(String field) {
            return String.format("%s changed. existing: '{}' -> new: '{}'", field);
        }
    }
}
