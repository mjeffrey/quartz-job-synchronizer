package be.sysa.quartz.initializer.fixtures.assertions.model;

import be.sysa.quartz.initializer.model.JobDefinition;
import be.sysa.quartz.initializer.model.TriggerDefinition;
import org.assertj.core.api.AbstractAssert;
import org.quartz.utils.Key;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class JobDefinitionAssert extends AbstractAssert<JobDefinitionAssert, JobDefinition> {

    public JobDefinitionAssert(JobDefinition actual) {
        super(actual, JobDefinitionAssert.class);
    }

    public JobDefinitionAssert jobClass(Class<?> jobClass) {
        return jobClass(jobClass.getName());
    }

    public JobDefinitionAssert jobClass(String jobClass) {
        isNotNull();
        assertThat(actual.getJobClass().getName()).isEqualTo(jobClass);
        return this;
    }

    public JobDefinitionAssert descriptionContains(String expected) {
        isNotNull();
        assertThat(actual.getDescription()).contains(expected);
        return this;
    }

    public JobDefinitionAssert noDescription() {
        isNotNull();
        assertThat(actual.getDescription()).isNull();
        return this;
    }

    public JobDefinitionAssert recover(boolean expected) {
        isNotNull();
        assertThat(actual.isRecover()).isEqualTo(expected);
        return this;
    }

    public JobDefinitionAssert durable(boolean expected) {
        isNotNull();
        assertThat(actual.isDurable()).isEqualTo(expected);
        return this;
    }

    public JobDefinitionAssert hasJobData(Map<String, Object> expected) {
        isNotNull();
        assertThat(actual.getJobDataMap()).isEqualTo(expected);
        return this;
    }


    public JobDefinitionAssert hasTriggers(String... triggers) {
        isNotNull();
        Collection<String> actualJobs = getTriggerNames();
        assertThat(actualJobs).containsExactlyInAnyOrder(triggers);
        return this;
    }

    public TriggerDefinitionAssert hasTrigger(String triggerName) {
        isNotNull();
        TriggerDefinition trigger = actual.getTriggers().entrySet()
                .stream()
                .filter(e -> Objects.equals(e.getKey().getName(), triggerName))
                .findFirst().map(Map.Entry::getValue).orElse(null);

        if (trigger == null) {
            fail("Expected %s to contain %s", getTriggerNames(), triggerName);
        }
        return new TriggerDefinitionAssert(trigger);
    }

    private Collection<String> getTriggerNames() {
        return actual.getTriggers().keySet().stream().map(Key::getName).collect(Collectors.toSet());
    }

}
