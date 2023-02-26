package be.sysa.quartz.initializer.fixtures.assertions.model;

import be.sysa.quartz.initializer.model.GroupDefinition;
import be.sysa.quartz.initializer.model.ScheduleDefinition;
import org.assertj.core.api.AbstractAssert;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class ScheduleDefinitionAssert extends AbstractAssert<ScheduleDefinitionAssert, ScheduleDefinition> {

    public ScheduleDefinitionAssert(ScheduleDefinition actual) {
        super(actual, ScheduleDefinitionAssert.class);
    }

    public ScheduleDefinitionAssert hasGroups(String... groups) {
        isNotNull();
        Collection<String> actualGroups = getGroupNames();
        assertThat(actualGroups).containsExactlyInAnyOrder(groups);
        return this;
    }

    public GroupDefinitionAssert hasGroup(String groupName) {
        isNotNull();
        GroupDefinition group = actual.getGroups().get(groupName);
        if (group==null){
            fail("Expected %s to contain %s", getGroupNames(), groupName);
        }
        return new GroupDefinitionAssert(group);
    }

    private Collection<String> getGroupNames() {
        return actual.getGroups().keySet();
    }
}
