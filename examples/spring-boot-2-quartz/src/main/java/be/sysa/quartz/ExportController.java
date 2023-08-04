package be.sysa.quartz;

import be.sysa.quartz.initializer.api.ScheduleDefinitionApi;
import be.sysa.quartz.initializer.model.Mapper;
import be.sysa.quartz.initializer.model.ScheduleDefinition;
import be.sysa.quartz.initializer.service.JobSynchronizer;
import be.sysa.quartz.initializer.service.ScheduleExporter;
import be.sysa.quartz.initializer.service.ScheduleLoader;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@RestController
@AllArgsConstructor
@Slf4j
public class ExportController {
    Scheduler scheduler;
    @GetMapping(path = "/schedule/export", produces = "application/yaml")
    public String exportSchedule(){
        ScheduleExporter scheduleExporter = new ScheduleExporter(scheduler);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        scheduleExporter.export(outputStream);
        return outputStream.toString(StandardCharsets.UTF_8);
    }
    @PostMapping(path = "/schedule/import", consumes = "application/yaml")
    public void importSchedule(@RequestBody String scheduleYaml){
        JobSynchronizer jobSynchronizer = new JobSynchronizer(scheduler);
        ScheduleDefinitionApi scheduleDefinitionApi = ScheduleLoader.loadSchedule(new ByteArrayInputStream(scheduleYaml.getBytes(StandardCharsets.UTF_8)));
        ScheduleDefinition scheduleDefinition = Mapper.toModel(scheduleDefinitionApi);
        jobSynchronizer.synchronizeSchedule(scheduleDefinition);
    }

    @SneakyThrows
    @PostMapping(path = "/schedule/pause")
    public void pauseSchedule(){
        scheduler.pauseAll();
        log.info("Scheduler Paused");
    }

    @SneakyThrows
    @PostMapping(path = "/schedule/resume")
    public void resumeSchedule(){
        scheduler.resumeAll();
        log.info("Scheduler Resumed");
    }

}
