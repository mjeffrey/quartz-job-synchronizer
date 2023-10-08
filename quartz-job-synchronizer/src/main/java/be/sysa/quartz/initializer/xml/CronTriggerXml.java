package be.sysa.quartz.initializer.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.List;

/**
 * Represents a Cron trigger in XML format.
 *
 * <p>A Cron trigger is used to schedule jobs based on a specified cron expression.
 *
 * <p>This class is annotated with @Value and @Builder annotations to provide
 * value-based object equality and convenient construction of instances.
 *
 * <p>It is also annotated with @JacksonXmlRootElement to specify the root element
 * name when serializing the object to XML using Jackson library.
 *
 * <p>The class provides the following properties:
 * - name: The name of the trigger.
 * - group: The group name of the trigger.
 * - description: A description of the trigger.
 * - jobName: The name of the associated job.
 * - jobGroup: The group name of the associated job.
 * - priority: The priority of the trigger.
 * - cronExpression: The cron expression that defines the schedule for the trigger.
 * - timeZone: The time zone used for evaluating the cron expression.
 * - misfireInstruction: The misfire instruction for the trigger.
 * - jobDataMap: A list of job data entries associated with the trigger.
 *
 * <p>This class also supports equality and hash code calculations based on the
 * explicitly included properties.
 */
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
