package be.sysa.quartz.initializer.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.List;

/**
 * Represents a job definition in XML format.
 */
@Value
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class JobXml {
    @EqualsAndHashCode.Include
    String name;
    @EqualsAndHashCode.Include
    String group;
    @JacksonXmlProperty(localName = "job-class")
    String jobClass;
    boolean durability;
    boolean recover;
    @JacksonXmlProperty(localName = "job-data-map")
    List<JobDataEntryXml> jobDataMap;
}
