package be.sysa.quartz.initializer;

import be.sysa.quartz.initializer.api.ScheduleDefinitionApi;
import be.sysa.quartz.initializer.model.GroupDefinition;
import be.sysa.quartz.initializer.model.JobDefinition;
import be.sysa.quartz.initializer.model.Mapper;
import be.sysa.quartz.initializer.model.ScheduleDefinition;
import be.sysa.quartz.initializer.service.JobSynchronizer;
import be.sysa.quartz.initializer.service.ScheduleLoader;
import be.sysa.quartz.initializer.service.ScheduleMerger;
import be.sysa.quartz.initializer.support.FileLoader;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerPlugin;

import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

/**
 * YamlJobSynchronizerPlugin is a class that implements the SchedulerPlugin interface for synchronizing job schedules
 * based on YAML configuration files.
 * <p>
 * The class provides the following functionality:
 * - Initializing the plugin with the plugin name, scheduler instance, and class load helper
 * - Starting the synchronization process by reading YAML files and synchronizing job schedules
 * - Checking for forbidden jobs based on configuration and removing them from synchronization
 * - Converting YAML files to ScheduleDefinition models
 * - Shutting down the plugin gracefully
 * <p>
 * Usage:
 * 1. Create an instance of YamlJobSynchronizerPlugin and set the necessary properties (fileNames, allowJobs)
 * 2. Initialize the plugin by calling the initialize() method with the plugin name, scheduler instance, and class load helper
 * 3. Start the synchronization process by calling the start() method
 * 4. The plugin will automatically synchronize the job schedules based on the specified YAML files and allowed jobs
 * 5. Optionally, handle any exceptions thrown during the synchronization process
 * 6. Shut down the plugin by calling the shutdown() method
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Slf4j
public class YamlJobSynchronizerPlugin implements SchedulerPlugin {
    @Setter
    private String fileNames;
    @Setter
    private String allowJobs;

    private List<String> yamlFiles;
    private List<String> allowedJobList;

    private String name;
    private Scheduler scheduler;
    private ClassLoadHelper classLoadHelper;


    @Override
    public void initialize(String name, Scheduler scheduler, ClassLoadHelper classLoadHelper) {
        this.name = name;
        this.scheduler = scheduler;
        this.classLoadHelper = classLoadHelper;
        yamlFiles = splitToList(fileNames);
        allowedJobList = splitToList(allowJobs);
    }


    private List<String> splitToList(String stringToSplit) {
        return stringToSplit == null
                ? emptyList()
                : Stream.of(stringToSplit.split(",")).map(String::trim).collect(Collectors.toList());
    }

    @SneakyThrows
    @Override
    public void start() {
        if (!yamlFiles.isEmpty()) {
            synchronizeJobSchedule();
        }
    }

    private void synchronizeJobSchedule() {
        log.info("Starting New/Changed Job Synchronization");
        JobSynchronizer jobSynchronizer = new JobSynchronizer(scheduler);

        ScheduleDefinition scheduleDefinition = ScheduleMerger.merge(yamlFiles.stream().map(this::toModel).collect(Collectors.toList()));
        assertNoInbuiltJobs(scheduleDefinition, allowedJobList);
        
        jobSynchronizer.synchronizeSchedule(scheduleDefinition);
        log.info("Completed New/Changed Job Synchronization");
    }

    private void assertNoInbuiltJobs(ScheduleDefinition definition, List<String> allowedJobList) {
        Set<String> forbiddenJobs = new HashSet<>();
        Map<String, GroupDefinition> g = definition.getGroups();
        for (GroupDefinition groupDefinition : g.values()) {
            Map<JobKey, JobDefinition> j = groupDefinition.getJobs();
            for (JobDefinition jobDefinition : j.values()) {
                Class<? extends Job> jobClass = jobDefinition.getJobClass();
                String aClassName = jobClass.getName();
                if (aClassName.startsWith("org.quartz")) {
                    forbiddenJobs.add(aClassName);
                }
            }
        }
        allowedJobList.forEach(forbiddenJobs::remove);
        if (!forbiddenJobs.isEmpty()) {
            throw new IllegalArgumentException("Quartz provided jobs specified in the configuration, some are potentially dangerous and are not permitted by default." +
                                               " If required use allowProvidedJobs. Forbidden Jobs: " + forbiddenJobs);
        }
    }

    @SneakyThrows
    private ScheduleDefinition toModel(String yamlFile) {
        try (InputStream inputStream = FileLoader.getClassLoadHelperResourceAsStream(classLoadHelper, yamlFile)) {
            ScheduleDefinitionApi loadSchedule = ScheduleLoader.loadSchedule(inputStream);
            return Mapper.toModel(loadSchedule);
        }
    }

    @Override
    public void shutdown() {
        log.debug("Plugin Shutdown");
    }

}
