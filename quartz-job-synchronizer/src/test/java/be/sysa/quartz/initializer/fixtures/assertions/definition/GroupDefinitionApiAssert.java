package be.sysa.quartz.initializer.fixtures.assertions.definition;

import be.sysa.quartz.initializer.api.GroupDefinitionApi;
import be.sysa.quartz.initializer.api.JobDefinitionApi;
import org.assertj.core.api.AbstractAssert;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class GroupDefinitionApiAssert extends AbstractAssert<GroupDefinitionApiAssert, GroupDefinitionApi> {

    public GroupDefinitionApiAssert(GroupDefinitionApi actual) {
        super(actual, GroupDefinitionApiAssert.class);
    }

    public GroupDefinitionApiAssert hasJobs(String... jobs) {
        isNotNull();
        Collection<String> actualGroups = getJobNames();
        assertThat(actualGroups).containsExactlyInAnyOrder(jobs);
        return this;
    }

    public JobDefinitionApiAssert hasJob(String jobName) {
        isNotNull();
        JobDefinitionApi job = actual.getJobs().stream().filter(g -> Objects.equals(g.getName(), jobName))
                .findFirst().orElse(null);
        if (job==null){
            fail("Expected %s to contain %s", getJobNames(), jobName);
        }
        return new JobDefinitionApiAssert(job);
    }


    private List<String> getJobNames() {
        return actual.getJobs().stream().map(JobDefinitionApi::getName).collect(Collectors.toList());
    }
}
