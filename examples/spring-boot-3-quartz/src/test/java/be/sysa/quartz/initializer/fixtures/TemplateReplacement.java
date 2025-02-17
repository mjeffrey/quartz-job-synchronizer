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

    private String processLines(String token, Object value, boolean removeOnly) {
        return template.lines()
                .map(line -> {
                    if (StringUtils.contains(line, token)) {
                        return (removeOnly || value == null) ? null : String.valueOf(value);
                    }
                    return line;
                })
                .filter(StringUtils::isNotBlank) // Filter out null or blank lines
                .reduce((line1, line2) -> line1 + System.lineSeparator() + line2)
                .orElse("");
    }

    private String replaceLine(String token, Object value) {
        return processLines(token, value, false);
    }

    private String removeLine(String token) {
        return processLines(token, null, true);
    }

    @Override
    public String toString() {
        return template;
    }

}
