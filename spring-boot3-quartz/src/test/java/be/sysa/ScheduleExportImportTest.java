package be.sysa;

import be.sysa.quartz.SpringBootV3QuartzApp;
import be.sysa.quartz.initializer.support.YamlObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.http.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@SpringBootTest(classes = SpringBootV3QuartzApp.class, webEnvironment= WebEnvironment.RANDOM_PORT)
public class ScheduleExportImportTest {

    TestRestTemplate restTemplate = new TestRestTemplate();

    @LocalServerPort
    int port;

    private String baseUrl() {
        return "http://localhost:" + port;
    }

    @SneakyThrows
    @Test
    public void importSchedule() {
        String importYaml = IOUtils.toString(new DefaultResourceLoader().getResource("classpath:jobs.yaml").getInputStream(), StandardCharsets.UTF_8);
        restTemplate.exchange(importRequest(importYaml), String.class);
        ResponseEntity<String> response = restTemplate.getForEntity(exportUri(),  String.class);
        assertYamlEquivalent(importYaml, response.getBody());
    }

    @SneakyThrows
    private void assertYamlEquivalent(String importYaml, String exportYaml) {
        ObjectMapper objectMapper = YamlObjectMapper.createObjectMapper();
        JsonNode imported = objectMapper.readTree(importYaml);
        JsonNode exported = objectMapper.readTree(exportYaml);
        JSONAssert.assertEquals(imported.toString(), exported.toString(), true);
    }

    private RequestEntity<String> importRequest(String yaml) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.valueOf("application/yaml"));
        return new RequestEntity<>(yaml, httpHeaders, HttpMethod.POST, importUri());
    }

    private URI exportUri() {
        return URI.create(baseUrl() + "/schedule/export");
    }

    private URI importUri() {
        return URI.create(baseUrl() + "/schedule/import");
    }

}
