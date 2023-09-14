package be.sysa.quartz.initializer.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Builder;
import lombok.Value;

/**
 * Represents a schedule in XML format. This is the root of the XML definitions.
 */
@Value
@Builder
@JacksonXmlRootElement(localName = "job-scheduling-data")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScheduleDataXml {
    ScheduleXml schedule;
}
