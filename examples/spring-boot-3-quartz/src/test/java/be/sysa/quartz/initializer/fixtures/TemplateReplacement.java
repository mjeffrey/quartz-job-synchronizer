package be.sysa.quartz.initializer.fixtures;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@AllArgsConstructor
public class TemplateReplacement {
    String template;

    public static String def(String value, String defaultValue) {
        return Optional.ofNullable(value).orElse(defaultValue);
    }

    public void fill(String token, Object value) {
        template = value == null
                ? removeLine(token)
                : StringUtils.replace(template, token, String.valueOf(value));
    }

    public void replaceAndRemoveLine(String token, Object value) {
        template = replaceLine(token, value);
    }

    private String replaceLine(String token, Object value) {
        String[] lines = template.split(System.lineSeparator());
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (StringUtils.contains(line, token)) {
                if (value != null) {
                    sb.append(value);
                }
            } else {
                sb.append(line).append(System.lineSeparator());
            }
        }
        return sb.toString();
    }

    private String removeLine(String token) {
        String[] lines = template.split(System.lineSeparator());
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (!StringUtils.contains(line, token)) {
                sb.append(line).append(System.lineSeparator());
            }
        }
        return sb.toString();
    }


    @Override
    public String toString() {
        return template;
    }

}
