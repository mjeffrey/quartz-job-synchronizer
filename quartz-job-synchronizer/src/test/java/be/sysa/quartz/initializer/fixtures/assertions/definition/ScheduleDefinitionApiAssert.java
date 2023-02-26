package be.sysa.quartz.initializer.fixtures.assertions.definition;

import be.sysa.quartz.initializer.api.GroupDefinitionApi;
import be.sysa.quartz.initializer.api.ScheduleDefinitionApi;
import org.assertj.core.api.AbstractAssert;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ScheduleDefinitionApiAssert extends AbstractAssert<ScheduleDefinitionApiAssert, ScheduleDefinitionApi> {

    public ScheduleDefinitionApiAssert(ScheduleDefinitionApi actual) {
        super(actual, ScheduleDefinitionApiAssert.class);
    }

    public ScheduleDefinitionApiAssert hasGroups(String... groups) {
        isNotNull();
        Collection<String> actualGroups = getGroupNames();
        assertThat(actualGroups).containsExactlyInAnyOrder(groups);
        return this;
    }

    public GroupDefinitionApiAssert hasGroup(String groupName) {
        isNotNull();
        GroupDefinitionApi group = actual.getGroups().stream().filter(g -> Objects.equals(g.getName(), groupName))
                .findFirst().orElse(null);
        if (group==null){
            fail("Expected %s to contain %s", getGroupNames(), groupName);
        }
        return new GroupDefinitionApiAssert(group);
    }

    private List<String> getGroupNames() {
        return actual.getGroups().stream().map(GroupDefinitionApi::getName).collect(Collectors.toList());
    }
}
