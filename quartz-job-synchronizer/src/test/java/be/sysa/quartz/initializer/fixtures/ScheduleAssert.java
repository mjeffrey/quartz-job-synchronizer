package be.sysa.quartz.initializer.fixtures;

import be.sysa.quartz.initializer.api.ScheduleDefinitionApi;
import be.sysa.quartz.initializer.fixtures.assertions.definition.ScheduleDefinitionApiAssert;
import be.sysa.quartz.initializer.fixtures.assertions.model.ScheduleDefinitionAssert;
import be.sysa.quartz.initializer.model.ScheduleDefinition;
import org.assertj.core.api.Assertions;

public class ScheduleAssert extends Assertions {
    public static ScheduleDefinitionApiAssert assertThat(ScheduleDefinitionApi actual) {
        return new ScheduleDefinitionApiAssert(actual);
    }

    public static ScheduleDefinitionAssert assertThat(ScheduleDefinition actual) {
        return new ScheduleDefinitionAssert(actual);
    }

}
