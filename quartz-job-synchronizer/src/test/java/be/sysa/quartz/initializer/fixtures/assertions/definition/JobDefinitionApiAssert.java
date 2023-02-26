package be.sysa.quartz.initializer.fixtures.assertions.definition;

import be.sysa.quartz.initializer.api.JobDefinitionApi;
import be.sysa.quartz.initializer.api.TriggerDefinitionApi;
import org.assertj.core.api.AbstractAssert;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class JobDefinitionApiAssert extends AbstractAssert<JobDefinitionApiAssert, JobDefinitionApi> {

    public JobDefinitionApiAssert(JobDefinitionApi actual) {
        super(actual, JobDefinitionApiAssert.class);
    }

    public JobDefinitionApiAssert jobClass(Class<?> jobClass) {
        return jobClass(jobClass.getName());
    }

    public JobDefinitionApiAssert jobClass(String jobClass) {
        isNotNull();
        assertThat(actual.getJobClass()).isEqualTo(jobClass);
        return this;
    }

    public JobDefinitionApiAssert descriptionContains(String expected) {
        isNotNull();
        assertThat(actual.getDescription()).contains(expected);
        return this;
    }

    public JobDefinitionApiAssert noDescription() {
        isNotNull();
        assertThat(actual.getDescription()).isNull();
        return this;
    }

    public JobDefinitionApiAssert recover(Boolean expected) {
        isNotNull();
        assertThat(actual.getRecover()).isEqualTo(expected);
        return this;
    }
    public JobDefinitionApiAssert durable(Boolean expected) {
        isNotNull();
        assertThat(actual.getDurable()).isEqualTo(expected);
        return this;
    }

    public JobDefinitionApiAssert hasJobData(Map<String, Object> expected) {
        isNotNull();
        assertThat(actual.getJobDataMap()).isEqualTo(expected);
        return this;
    }


    public JobDefinitionApiAssert hasTriggers(String... triggers) {
        isNotNull();
        Collection<String> actualJobs = getTriggerNames();
        assertThat(actualJobs).containsExactlyInAnyOrder(triggers);
        return this;
    }

    public TriggerDefinitionApiAssert hasTrigger(String triggerName) {
        isNotNull();
        TriggerDefinitionApi trigger = actual.getTriggers().stream().filter(g -> Objects.equals(g.getName(), triggerName))
                .findFirst().orElse(null);
        if (trigger==null){
            fail("Expected %s to contain %s", getTriggerNames(), triggerName);
        }
        return new TriggerDefinitionApiAssert(trigger);
    }

    private List<String> getTriggerNames() {
        return actual.getTriggers().stream().map(TriggerDefinitionApi::getName).collect(Collectors.toList());
    }


}
