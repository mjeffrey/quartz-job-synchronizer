package be.sysa.quartz.initializer.fixtures;

import be.sysa.quartz.initializer.api.JobDefinitionApi;
import be.sysa.quartz.initializer.api.TriggerDefinitionApi;

public class ScheduleFixture {
    public static JobDefinitionApi maximalJob(){
        return JobDefinitionApi.builder()
                .name("MaximalJob")
                .recover(true)
                .jobClass("be.sysa.quartz.initializer.fixtures.jobs.MyTestJob")
                .jobData("Key1", "value1")
                .jobData("Key2", 2)
                .trigger(maximalTrigger())
                .build();
    }
    public static TriggerDefinitionApi maximalTrigger(){
        return TriggerDefinitionApi.builder()
                .name("FileGeneration")
                .cronExpression("0 1/5 6-20 ? * MON,TUE,WED,THU,FRI *")
                .cronExpression("0 0 2 ? * MON,TUE,WED,THU,FRI *")
                .timeZone("Europe/Brussels")
                .priority(10)
                .description("every 5 minutes starting at minute 02, every hour between 06:00 and 20:00, every Weekday")
                .triggerGroup("triggerGroup")
                .misfireExecution(true)
                .jobData("datakey1", "valueX")
                .jobData("datakey2", 0.123)
                .build();
    }
    public static JobDefinitionApi mimimalJob() {
        return JobDefinitionApi.builder()
                .name("MinimalJob")
                .jobClass("be.sysa.quartz.initializer.fixtures.jobs.MyTestJob")
                .trigger(minimalTrigger())
                .build();
    }

    public static TriggerDefinitionApi minimalTrigger() {
        return TriggerDefinitionApi.builder()
                .name("FileGeneration")
                .cronExpression("0 0 2 ? * MON,TUE,WED,THU,FRI *")
                .build();
    }
}
