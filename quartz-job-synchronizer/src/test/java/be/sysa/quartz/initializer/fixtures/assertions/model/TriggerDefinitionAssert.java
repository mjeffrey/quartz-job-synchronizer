package be.sysa.quartz.initializer.fixtures.assertions.model;

import be.sysa.quartz.initializer.model.TriggerDefinition;
import org.assertj.core.api.AbstractAssert;

import java.util.Map;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

public class TriggerDefinitionAssert extends AbstractAssert<TriggerDefinitionAssert, TriggerDefinition> {

    public TriggerDefinitionAssert(TriggerDefinition actual) {
        super(actual, TriggerDefinitionAssert.class);
    }

    public TriggerDefinitionAssert timeZone(String timeZone) {
        isNotNull();
        TimeZone expected = TimeZone.getTimeZone(timeZone);
        assertThat(actual.getTimeZone()).isEqualTo(expected);
        return this;
    }

    public TriggerDefinitionAssert triggerGroup(String triggerGroup) {
        isNotNull();
        assertThat(actual.getTriggerKey().getGroup()).isEqualTo(triggerGroup);
        return this;
    }

    public TriggerDefinitionAssert priority(Integer priority) {
        isNotNull();
        assertThat(actual.getPriority()).isEqualTo(priority);
        return this;
    }

    public TriggerDefinitionAssert descriptionContains(String expected) {
        isNotNull();
        assertThat(actual.getDescription()).contains(expected);
        return this;
    }

    public TriggerDefinitionAssert noDescription() {
        isNotNull();
        assertThat(actual.getDescription()).isNull();
        return this;
    }

    public TriggerDefinitionAssert misfireExecution(boolean expected) {
        isNotNull();
        assertThat(actual.isMisfireExecution()).isEqualTo(expected);
        return this;
    }

    public TriggerDefinitionAssert hasJobData(Map<String, Object> expected) {
        isNotNull();
        assertThat(actual.getJobDataMap()).isEqualTo(expected);
        return this;
    }

    public TriggerDefinitionAssert cronExpression(String cronExpression) {
        isNotNull();
        assertThat(actual.getCronExpression()).isEqualTo(cronExpression);
        return this;
    }



}
