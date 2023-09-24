package be.sysa.quartz.initializer.service;

import be.sysa.quartz.initializer.api.GroupDefinitionApi;
import be.sysa.quartz.initializer.api.JobDefinitionApi;
import be.sysa.quartz.initializer.api.ScheduleDefinitionApi;
import be.sysa.quartz.initializer.fixtures.ScheduleFixture;
import be.sysa.quartz.initializer.model.Mapper;
import be.sysa.quartz.initializer.model.ScheduleDefinition;
import be.sysa.quartz.initializer.support.Errors;
import be.sysa.quartz.initializer.support.ScheduleDefinitionException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ScheduleMergerTest {


    @Test
    @DisplayName("Same group name is not permitted")
    public void duplicateGroup() {
        ScheduleDefinition schedule1 = groupWithJob("g1", ScheduleFixture.maximalJob());
        ScheduleDefinition schedule2 = groupWithJob("g1", ScheduleFixture.maximalJob());

        assertThatExceptionOfType(ScheduleDefinitionException.class)
                .isThrownBy(() -> ScheduleMerger.merge(List.of(schedule1, schedule2)))
                .extracting(ScheduleDefinitionException::getErrorCode).isEqualTo(Errors.DUPLICATE_GROUP)
        ;
    }

    @Test
    @DisplayName("Using a different group with the same job is OK for the Job since the group is part of the key. Trigger is then duplicated")
    public void duplicateTrigger() {
        ScheduleDefinition schedule1 = groupWithJob("g1", ScheduleFixture.maximalJob());
        ScheduleDefinition schedule2 = groupWithJob("g2", ScheduleFixture.maximalJob());

        assertThatExceptionOfType(ScheduleDefinitionException.class)
                .isThrownBy(() -> ScheduleMerger.merge(List.of(schedule1, schedule2)))
                .extracting(ScheduleDefinitionException::getErrorCode).isEqualTo(Errors.DUPLICATE_TRIGGER)
        ;
    }

    @Test
    @DisplayName("Using a different group with the different job iS OK")
    public void mergeOk() {
        ScheduleDefinition schedule1 = groupWithJob("g1", ScheduleFixture.maximalJob());
        ScheduleDefinition schedule2 = groupWithJob("g2", ScheduleFixture.minimalJob());
        ScheduleDefinition merged = ScheduleMerger.merge(List.of(schedule1, schedule2));
        assertThat(merged.getGroups().keySet())
                .containsExactly("g1", "g2")
        ;
    }

    @Test
    @DisplayName("Empty Schedule List")
    public void noSchedules() {
        assertThatExceptionOfType(ScheduleDefinitionException.class)
                .isThrownBy(() -> ScheduleMerger.merge(Collections.emptyList()))
                .extracting(ScheduleDefinitionException::getErrorCode).isEqualTo(Errors.NO_SCHEDULES)
        ;
    }


    private static ScheduleDefinition groupWithJob(String groupName, JobDefinitionApi job) {
        GroupDefinitionApi groupDefinition = GroupDefinitionApi.builder()
                .name(groupName)
                .job(job)
                .build();
        return Mapper.toModel(ScheduleDefinitionApi.builder().group(groupDefinition).build());
    }


}
