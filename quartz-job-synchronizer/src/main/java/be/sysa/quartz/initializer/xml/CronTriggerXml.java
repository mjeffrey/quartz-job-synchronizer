package be.sysa.quartz.initializer.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.List;

@Value
@Builder
@JacksonXmlRootElement(localName = "cron")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class CronTriggerXml {
    @EqualsAndHashCode.Include
    String name;
    @EqualsAndHashCode.Include
    String group;
    String description;
    @JacksonXmlProperty(localName = "job-name")
    String jobName;
    @JacksonXmlProperty(localName = "job-group")
    String jobGroup;

    Integer priority;

    @JacksonXmlProperty(localName = "cron-expression")
    String cronExpression;

    @JacksonXmlProperty(localName = "time-zone")
    String timeZone;

    @JacksonXmlProperty(localName = "misfire-instruction")
    String misfireInstruction;

    @JacksonXmlProperty(localName = "job-data-map")
    List<JobDataEntryXml> jobDataMap;
}
