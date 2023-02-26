package be.sysa.quartz.initializer.fixtures.assertions.definition;

import be.sysa.quartz.initializer.api.TriggerDefinitionApi;
import org.assertj.core.api.AbstractAssert;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class TriggerDefinitionApiAssert extends AbstractAssert<TriggerDefinitionApiAssert, TriggerDefinitionApi> {

    public TriggerDefinitionApiAssert(TriggerDefinitionApi actual) {
        super(actual, TriggerDefinitionApiAssert.class);
    }

    public TriggerDefinitionApiAssert timeZone(String triggerClass) {
        isNotNull();
        assertThat(actual.getTimeZone()).isEqualTo(triggerClass);
        return this;
    }

    public TriggerDefinitionApiAssert triggerGroup(String triggerGroup) {
        isNotNull();
        assertThat(actual.getTriggerGroup()).isEqualTo(triggerGroup);
        return this;
    }

    public TriggerDefinitionApiAssert cronExpressions(String... cronExpressions) {
        isNotNull();
        assertThat(actual.getCronExpressions()).containsExactlyInAnyOrder(cronExpressions);
        return this;
    }

    public TriggerDefinitionApiAssert priority(Integer priority) {
        isNotNull();
        assertThat(actual.getPriority()).isEqualTo(priority);
        return this;
    }

    public TriggerDefinitionApiAssert descriptionContains(String expected) {
        isNotNull();
        assertThat(actual.getDescription()).contains(expected);
        return this;
    }

    public TriggerDefinitionApiAssert noDescription() {
        isNotNull();
        assertThat(actual.getDescription()).isNull();
        return this;
    }

    public TriggerDefinitionApiAssert misfireExecution(Boolean expected) {
        isNotNull();
        assertThat(actual.getMisfireExecution()).isEqualTo(expected);
        return this;
    }

    public TriggerDefinitionApiAssert hasJobData(Map<String, Object> expected) {
        isNotNull();
        assertThat(actual.getJobDataMap()).isEqualTo(expected);
        return this;
    }


}
