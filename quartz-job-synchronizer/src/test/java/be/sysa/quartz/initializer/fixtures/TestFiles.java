package be.sysa.quartz.initializer.fixtures;

import lombok.SneakyThrows;

import java.io.InputStream;
import java.net.URL;

public class TestFiles {

    public static InputStream xmlToBeConverted() {
        return fromTestFile("jobs-in-xml.xml");
    }

    public static InputStream xmlExpectedConvertedToYaml() {
        return fromTestFile("jobs-from-xml.yaml");
    }

    public static InputStream yamlJobs() {
        return fromTestFile("jobs.yaml");
    }

    @SneakyThrows
    private static InputStream fromTestFile(String testFile) {
        URL resource = TestFiles.class.getClassLoader().getResource(testFile);
        if (resource == null) {
            throw new IllegalStateException("Cannot open expected test file " + testFile);
        }
        return resource.openStream();
    }
}
