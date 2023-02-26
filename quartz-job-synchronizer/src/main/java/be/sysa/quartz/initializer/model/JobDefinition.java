package be.sysa.quartz.initializer.model;


import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import org.quartz.Job;
import org.quartz.JobKey;
import org.quartz.TriggerKey;

import java.util.Map;

@Value
@Builder(toBuilder = true)
public class JobDefinition {

    @NonNull JobKey  jobKey;
    @NonNull Class<? extends Job> jobClass;

    String description;

    boolean recover;

    boolean durable;

    @NonNull
    Map<TriggerKey, TriggerDefinition> triggers;

    Map<String, Object> jobDataMap;
}
