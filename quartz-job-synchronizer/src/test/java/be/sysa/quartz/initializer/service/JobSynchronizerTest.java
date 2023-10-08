package be.sysa.quartz.initializer.service;

import be.sysa.quartz.initializer.api.GroupDefinitionApi;
import be.sysa.quartz.initializer.api.OptionsDefinitionApi;
import be.sysa.quartz.initializer.api.ScheduleDefinitionApi;
import be.sysa.quartz.initializer.model.Mapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;

import static be.sysa.quartz.initializer.fixtures.ScheduleFixture.maximalJob;
import static be.sysa.quartz.initializer.fixtures.ScheduleFixture.minimalJob;
import static be.sysa.quartz.initializer.service.ScheduleLoader.writeString;

public class JobSynchronizerTest {

    @Test
    public void synchronizeSchedule() {
        JobSynchronizer jobSynchronizer = new JobSynchronizer(getScheduler());
        GroupDefinitionApi maximal = GroupDefinitionApi.builder().name("maximal").job(maximalJob()).build();
        GroupDefinitionApi minimal = GroupDefinitionApi.builder().name("minimal").job(minimalJob()).build();
        OptionsDefinitionApi options = OptionsDefinitionApi.builder().groupToDelete("CCC").build();
        ScheduleDefinitionApi scheduleDefinition = ScheduleDefinitionApi.builder()
                .group(maximal)
                .group(minimal)
                .options(options)
                .build();

        System.out.println(writeString(scheduleDefinition));
        jobSynchronizer.synchronizeSchedule(Mapper.toModel(scheduleDefinition));
    }


    @SneakyThrows
    Scheduler getScheduler() {
        // Create a default instance of the Scheduler
        return StdSchedulerFactory.getDefaultScheduler();
    }
    @Test
    public void replaceJob() {
        JobSynchronizer jobSynchronizer = new JobSynchronizer(getScheduler());
        GroupDefinitionApi maximal1 = GroupDefinitionApi.builder().name("maximal").job(maximalJob()).build();
        GroupDefinitionApi maximal2 =  maximal1.toBuilder().clearJobs().job(maximalJob().toBuilder().recover(true).build()).build();
        ScheduleDefinitionApi scheduleDefinition1 = ScheduleDefinitionApi.builder().group(maximal1).build();
        ScheduleDefinitionApi scheduleDefinition2 = ScheduleDefinitionApi.builder().group(maximal1).build();

        System.out.println(writeString(scheduleDefinition1));
        jobSynchronizer.synchronizeSchedule(Mapper.toModel(scheduleDefinition1));
        System.out.println(writeString(scheduleDefinition2));
        jobSynchronizer.synchronizeSchedule(Mapper.toModel(scheduleDefinition1));
        // TODO add assertions

    }

}
