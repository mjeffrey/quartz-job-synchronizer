package be.sysa.quartz.initializer.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Builder;
import lombok.Value;

/**
 * Represents a single entry of job data in XML format.
 */
@Value
@Builder
@JacksonXmlRootElement(localName = "entry")
public class JobDataEntryXml {
    String key;
    String value;
}
