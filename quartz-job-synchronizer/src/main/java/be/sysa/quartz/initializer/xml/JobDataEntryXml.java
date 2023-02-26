package be.sysa.quartz.initializer.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@JacksonXmlRootElement(localName = "entry")
public class JobDataEntryXml {
    String key;
    String value;
}
