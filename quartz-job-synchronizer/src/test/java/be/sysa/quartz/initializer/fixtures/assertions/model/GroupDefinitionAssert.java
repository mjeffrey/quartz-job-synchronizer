package be.sysa.quartz.initializer.fixtures.assertions.model;

import be.sysa.quartz.initializer.model.GroupDefinition;
import be.sysa.quartz.initializer.model.JobDefinition;
import org.assertj.core.api.AbstractAssert;
import org.quartz.utils.Key;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class GroupDefinitionAssert extends AbstractAssert<GroupDefinitionAssert, GroupDefinition> {

    public GroupDefinitionAssert(GroupDefinition actual) {
        super(actual, GroupDefinitionAssert.class);
    }

    public GroupDefinitionAssert hasJobs(String... jobs) {
        isNotNull();
        Collection<String> actualGroups = getJobNames();
        assertThat(actualGroups).containsExactlyInAnyOrder(jobs);
        return this;
    }

    public JobDefinitionAssert hasJob(String jobName) {
        isNotNull();
        JobDefinition job = actual.getJobs().entrySet()
                .stream()
                .filter(e-> Objects.equals(e.getKey().getName(),jobName))
                .findFirst().map(Map.Entry::getValue).orElse(null);
        if (job == null) {
            fail("Expected %s to contain %s", getJobNames(), jobName);
        }
        return new JobDefinitionAssert(job);
    }


    private Collection<String> getJobNames() {
        return actual.getJobs().keySet().stream().map(Key::getName).collect(Collectors.toSet());
    }
}
