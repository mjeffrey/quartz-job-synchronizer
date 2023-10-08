package be.sysa.quartz.initializer.fixtures;

import be.sysa.quartz.jobs.SampleJob;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import static be.sysa.quartz.initializer.fixtures.TemplateReplacement.def;


@Slf4j
public class JobTemplate {
    private static final String maximalJob = """
                  - job-name: "${job-name}"
                    job-class: "${job-class}"
                    recover: ${recover}
                    durable: ${durable}
                    ${job-data-map}
                    ${dependency1}
                    ${dependency2}
                    triggers:
                      ${trigger1}
                      ${trigger2}
            """;
    public static final String DEFAULT_CLASS = SampleJob.class.getName();

    private final TemplateReplacement template;


    @Builder
    public JobTemplate(String jobName,
                       String jobClass,
                       Boolean recover,
                       Boolean durable,
                       JobDataMapTemplate jobDataMap,
                       TriggerTemplate trigger1,
                       TriggerTemplate trigger2,
                       JobTemplate dependency1,
                       JobTemplate dependency2
    ) {
        template = new TemplateReplacement(maximalJob);
        template.fill("${job-name}", def(jobName, "job-name"));
        template.fill("${job-class}", def(jobClass, DEFAULT_CLASS));
        template.fill("${recover}", recover);
        template.fill("${durable}", durable);
        template.replaceAndRemoveLine("${job-data-map}", jobDataMap);
        template.replaceAndRemoveLine("${trigger1}", trigger1);
        template.replaceAndRemoveLine("${trigger2}", trigger2);
        template.replaceAndRemoveLine("${dependency1}", dependency1);
        template.replaceAndRemoveLine("${dependency2}", dependency2);
        log.debug("job template {}", template);
    }

    @Override
    public String toString() {
        return template.toString();
    }

}
