package be.sysa.quartz.initializer;

import be.sysa.quartz.initializer.service.ScheduleExporter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * YamlScheduleExporterPlugin is a scheduler plugin that exports the existing job schedules to a YAML file.
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Slf4j
public class YamlScheduleExporterPlugin implements SchedulerPlugin {
    @Setter
    private String exportFile;

    private String name;
    private Scheduler scheduler;
    private ClassLoadHelper classLoadHelper;


    @Override
    public void initialize(String name, Scheduler scheduler, ClassLoadHelper classLoadHelper) {
        this.name = name;
        this.scheduler = scheduler;
        this.classLoadHelper = classLoadHelper;
    }

    @SneakyThrows
    @Override
    public void start() {
        exportExistingJobSchedule();
    }

    private void exportExistingJobSchedule() throws IOException {
        log.info("Starting YAML file export");
        ScheduleExporter scheduleExporter = new ScheduleExporter(scheduler);
        File file = new File(exportFile);
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            scheduleExporter.export(fileOutputStream);
        }
        log.info("Exported YAML file {}", file.getAbsolutePath());
    }

    @Override
    public void shutdown() {
        log.debug("Plugin Shutdown");
    }

}
