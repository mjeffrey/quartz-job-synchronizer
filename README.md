# Quartz YAML Job Synchronizer

This is a Quartz Scheduler plugin to maintain jobs and triggers. 
It is meant as a replacement for Quartz XML Job plugin provided by Quartz and uses the same concepts but improves on it and
has simpler more logical configuration less prone to errors or unexpected consequences.

### Features:
- Only replaces Jobs in a group if they have really changed
  - If the job itself has changed, it is replaced along with its triggers (update is not an option in Quartz)
  - If the definition has not changed then no changes are made to the schedule
  - If the definition no longer exists, the job is removed (along with its triggers)
- Only replaces Triggers if they have changed
  - If one trigger changes but the job stays the same, only the trigger is replaced.
  - If the trigger is to be replaced **and** has missed its scheduled execution time **and** the trigger is configured to execute misfired triggers then an execution is scheduled immediatley

- Can export the schedule (no need to write it by hand): the YAML file can be generated from existing jobs and triggers
- Can convert an exising XML file to a YAML file.
- The YAML structure is less verbose (approx half the number of lines)
- Triggers are listed hierarchically under the jobs they belong to (triggers and jobs are inconveniently separate in XML plugin)
- There is an optional CRON description feature: the CRON expression is described in human terms.
- Each trigger can have multiple cron expressions (during parsing there will be created as separate triggers)
- Has sensible defaults
  - By default, triggers use the same group name as the job they belong to.
  - If no trigger description is provided, the cron description is used (needs the cron-description jar)
- There is a JSON schema so the IDE can help write and validate the file (the XML plugin also has this)
- Simplified "misfire" handling: true or false

## Problems with the XML plugin
The provided XML plugin has some problems when you want to modify jobs or their schedule that can result in missed or unwanted executions. 

The plugin will only add/replace jobs and triggers, if you change the name of a Job you will effectively have a duplicate with unforeseen consequences.
The same applies to a trigger, renaming a trigger will mean you have multiple triggers. 
Since the trigger is replaced each time, any triggers that have miss their fire time are deleted (and not executed).

To work around this, it is possible to just delete all jobs and then add all jobs again.
This works, but it can result in missed executions or deleting a misfired trigger or even a job being executed.
In a cloud environment where there are multiple elastic instances this can be happening often.

## Improvements
This plugin addresses this issue by only replacing things that change. And if we need to replace a job or trigger we handle any misfires.

### Limitations:
The YAML plugin does not support the following XMl plugin features:
- Refresh of the schedule by changing the XML file (this only works if the file is on the file system anyway)
- Triggers other than CRON but any (configurable) trigger can be implemented as a CRON trigger

## Installing
The easiest way to use quartz-job-synchronizer is with Maven or Gradle.

## Requirements
Java11. If you really need Java8 it can be converted pretty easily.
Some Jackson Additional Dependencies 
- jackson-databind (and core)
- jackson-dataformat-yaml 
- jackson-dataformat-xml (if XML->YAML conversion is used)
- cron-expression-descriptor (if using the human cron description feature)

### Maven:
```xml
<dependency>
    <groupId>be.sysa.quartz-job-synchronizer</groupId>
    <artifactId>quartz-job-synchronizer</artifactId>
    <version>1.0.0</version>
</dependency>
<dependency>
    <groupId>be.sysa.quartz-job-synchronizer</groupId>
    <artifactId>cron-description</artifactId>
    <version>1.0.0</version>
</dependency>
```
### Gradle:
```gradle
compile group: 'be.sysa', name: 'quartz-job-synchronizer-core', version: '1.0.5'
compile group: 'be.sysa', name: 'quartz-job-synchronizer-logback', version: '1.0.5'
```
# Getting Started
This plugin is a direct replacement for the `XMLSchedulingDataProcessorPlugin` provided with quartz, so while examples are using Spring Boot , any documentation for setting up the XML Plugin on other platforms will be applicable to these YAML plugins.


## Exporting a schedule.
First you need a YAML file. The easiest way is to convert from your existing schedule. Or write one by hand using one of the examples here. 
```yaml
    yamlScheduleExporter:
      class: "be.sysa.quartz.initializer.YamlScheduleExporterPlugin"
      exportFile: "${java.io.tmpdir}/schedule-export.yaml"
```

## Setting up the synchronization.
The Synchronizer plugin only needs 1 parameter, a list of YAML files defining
the jobs and their triggers. If you use NativeJob (or other Quartz provided plugins which are risky, then they need to be configured).
Disabling unsafe jobs by default is a means to prevent injection attacks if the schedule is loaded by another mechanism.
```yaml
    jobSynchronizer:
      class: "be.sysa.quartz.initializer.YamlJobSynchronizerPlugin"
      fileNames: "classpath:jobs-group1.yaml, classpath:jobs/other-jobs.yaml" 
      allowJobs: "org.quartz.NativeJob"
```

## Setting up the synchronization.
An alternative to exporting the full schedule is to just convert one file.
```yaml
    XmlYamlConverter:
      class: "be.sysa.quartz.initializer.XmlYamlConverterPlugin"
      importFile: "classpath:conversion-jobs.xml"
      exportFile: "${java.io.tmpdir}/conversion-jobs.yaml"
```


# Development

## Running the tests

./mvnw clean verify

## Running the JMH benchmarks

./mvnw clean verify

## Caveats and Limitations
The Logback sanitizer Has been used in production in a PCI-DSS compliant system for almost 1 year. 

## Contributing

Please read [CONTRIBUTING.md](https://gist.github.com/PurpleBooth/b24679402957c63ec426) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/your/project/tags). 

## Authors

* **Mark Jeffrey** 

## License

This project is licensed under the Apache 2.0 License - see the [LICENSE.txt](LICENSE.txt) file for details

## Acknowledgments
