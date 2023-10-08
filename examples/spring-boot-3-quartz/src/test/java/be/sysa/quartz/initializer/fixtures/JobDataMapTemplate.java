package be.sysa.quartz.initializer.fixtures;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class JobDataMapTemplate {
    private static final String forJob = """
                            job-data-map:
                              ${key1}: "${value1}"
                    """;
    public static final String forTrigger = """
                                job-data-map:
                                  ${key1}: "${value1}"
                                  ${key2}: "${value2}"
                    """;

    private final TemplateReplacement template;

    public static JobDataMapTemplate.JobDataMapTemplateBuilder forJob() {
        return JobDataMapTemplate.builder().templateString(forJob);
    }

     public static JobDataMapTemplate.JobDataMapTemplateBuilder forTrigger() {
        return JobDataMapTemplate.builder().templateString(forTrigger);
    }

    @Builder
    public JobDataMapTemplate(String templateString,
                              String key1, String value1,
                              String key2, String value2
    ) {
        template = new TemplateReplacement(templateString);
        template.fill("${key1}", key1);
        template.fill("${value1}", value1);
        template.fill("${key2}", key2);
        template.fill("${value2}", value2);
        log.debug("job data map template {}", template);
    }

    @Override
    public String toString() {
        return template.toString();
    }

}
