package be.sysa.quartz.initializer.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TriggerXml {
    @JacksonXmlProperty(localName = "cron")
    CronTriggerXml cronTriggerXml;
}
