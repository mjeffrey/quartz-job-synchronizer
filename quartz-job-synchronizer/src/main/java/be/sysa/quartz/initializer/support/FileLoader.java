package be.sysa.quartz.initializer.support;

import lombok.SneakyThrows;
import org.quartz.spi.ClassLoadHelper;

import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileLoader {
    @SneakyThrows
    public static InputStream getClassLoadHelperResourceAsStream(ClassLoadHelper classLoadHelper, String resourceName) {
        InputStream stream = classLoadHelper.getResourceAsStream(resourceName);
        if (stream == null) {
            throw new FileNotFoundException("Cannot read from File or Resource: '" + resourceName + "'");
        }
        return stream;
    }
}
