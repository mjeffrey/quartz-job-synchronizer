package be.sysa.quartz.initializer.xml;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Builder;
import lombok.Value;

@Value
    @Builder
    @JacksonXmlRootElement(localName = "job-scheduling-data")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class ScheduleDataXml {

        ScheduleXml schedule;
    }
