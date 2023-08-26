package be.sysa.quartz.initializer;

import be.sysa.quartz.initializer.api.ScheduleDefinitionApi;
import be.sysa.quartz.initializer.service.ScheduleLoader;
import be.sysa.quartz.initializer.service.XmlScheduleLoader;
import be.sysa.quartz.initializer.support.FileLoader;
import be.sysa.quartz.initializer.xml.ScheduleDataXml;
import be.sysa.quartz.initializer.xml.XmlMapper;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.SchedulerPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

@Getter
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@Slf4j
public class XmlYamlConverterPlugin implements SchedulerPlugin {
    @Setter
    private String exportFile;
    @Setter
    private String importFile;

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
        log.info("Starting XML->File conversion");
        try( InputStream inputStream = FileLoader.getClassLoadHelperResourceAsStream(classLoadHelper, importFile)){
            ScheduleDataXml scheduleDataXml = XmlScheduleLoader.loadSchedule(inputStream);
            ScheduleDefinitionApi scheduleDefinitionApi = XmlMapper.toModel(scheduleDataXml);
            File exportFile = new File(this.exportFile);
            try (FileOutputStream fileOutputStream = new FileOutputStream(exportFile)) {
                ScheduleLoader.writeStream(fileOutputStream, scheduleDefinitionApi);
            }
            log.info("Exported YAML file {}", exportFile.getAbsolutePath());
        }
    }

    @Override
    public void shutdown() {
        log.debug("Plugin Shutdown");
    }

}
