package be.sysa.quartz.initializer;

import be.sysa.quartz.initializer.api.ScheduleDefinitionApi;
import be.sysa.quartz.initializer.model.GroupDefinition;
import be.sysa.quartz.initializer.model.JobDefinition;
import be.sysa.quartz.initializer.model.Mapper;
import be.sysa.quartz.initializer.model.ScheduleDefinition;
import be.sysa.quartz.initializer.service.JobSynchronizer;
import be.sysa.quartz.initializer.service.ScheduleLoader;
import be.sysa.quartz.initializer.support.FileLoader;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerPlugin;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

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
        List<ScheduleDefinition> definitions = yamlFiles.stream().map(this::toModel).collect(Collectors.toList());
        assertNoInbuiltJobs(definitions, allowedJobList);
        definitions.forEach(jobSynchronizer::synchronizeSchedule);
        log.info("Completed New/Changed Job Synchronization");
    }

    private void assertNoInbuiltJobs(List<ScheduleDefinition> definitions, List<String> allowedJobList) {
        Set<String> forbiddenJobs = definitions.stream().map(ScheduleDefinition::getGroups)
                .flatMap(g -> g.values().stream())
                .map(GroupDefinition::getJobs)
                .flatMap(j -> j.values().stream())
                .map(JobDefinition::getJobClass)
                .map(Class::getName)
                .filter(j -> j.startsWith("org.quartz"))
                .collect(Collectors.toSet());
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
