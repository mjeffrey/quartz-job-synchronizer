package be.sysa.quartz.initializer.fixtures;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import static be.sysa.quartz.initializer.fixtures.TemplateReplacement.def;

@Slf4j
public class TriggerTemplate {

    public static final String trigger = """
                      - name: "${trigger-name}"
                        trigger-group: "${trigger-group}"
                        misfire-execution: ${misfire-execution}
                        description: "${description}"
                        priority: ${priority}
                        expressions:
                          - "${cron1}"
                          - "${cron2}"
                        time-zone: "Europe/Brussels"
                        ${job-data-map}
            """;
    private final TemplateReplacement template;

    @Builder
    public TriggerTemplate(String triggerName,
                           String triggerGroup,
                           Boolean misfireExecution,
                           String description,
                           Integer priority,
                           String cron1,
                           String cron2,
                           JobDataMapTemplate jobDataMap
    ) {
        template = new TemplateReplacement(trigger);
        template.fill("${trigger-name}", def(triggerName, "trigger-name"));
        template.fill("${trigger-group}", triggerGroup);
        template.fill("${misfire-execution}", misfireExecution);
        template.fill("${description}", description);
        template.fill("${priority}", priority);
        template.replaceAndRemoveLine("${job-data-map}", jobDataMap);

        template.fill("${cron1}", cron1);
        template.fill("${cron2}", cron2);
        log.debug("trigger template {}", template);
    }

    @Override
    public String toString() {
        return template.toString();
    }

}
