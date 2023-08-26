package be.sysa.quartz.initializer.fixtures;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;


@Slf4j
public class ScheduleTemplate {
    private final String schedule = """
            options:
              groups-to-delete:
                - "Obsolete Group"
                            
            schedule:
            ${group1}
            ${group2}
            """;

    private final TemplateReplacement template;

    @Builder
    public ScheduleTemplate(GroupTemplate group1, GroupTemplate group2) {
        template = new TemplateReplacement(schedule);
        template.replaceAndRemoveLine("${group1}", group1);
        template.replaceAndRemoveLine("${group2}", group2);
        log.debug("schedule template {}", template);
    }

    @Override
    public String toString() {
        return template.toString();
    }

    public InputStream inputStream(){
        return new ByteArrayInputStream(template.toString().getBytes());
    }
}
