package be.sysa.quartz.initializer.fixtures;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import static be.sysa.quartz.initializer.fixtures.TemplateReplacement.def;


@Slf4j
public class GroupTemplate {
    private static final String group = """
              - group: "${group-name}"
                jobs:
                  ${job1}
                  ${job2}
            """;

    private final TemplateReplacement template;

    @Builder
    public GroupTemplate(String groupName, JobTemplate job1, JobTemplate job2) {
        template = new TemplateReplacement(group);
        template.fill("${group-name}", def(groupName, "group-name"));
        template.replaceAndRemoveLine("${job1}", job1);
        template.replaceAndRemoveLine("${job2}", job2);
        log.debug("group template {}", template);
    }

    @Override
    public String toString() {
        return template.toString();
    }

}
