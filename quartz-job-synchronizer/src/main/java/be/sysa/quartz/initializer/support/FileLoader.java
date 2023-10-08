package be.sysa.quartz.initializer.support;

import lombok.SneakyThrows;
import org.quartz.spi.ClassLoadHelper;

import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * A utility class for loading resources from class loaders.
 */
public class FileLoader {
    /**
     * Retrieves the input stream for the requested resource using the provided ClassLoadHelper object.
     *
     * @param classLoadHelper The ClassLoadHelper object to use for retrieving the resource.
     * @param resourceName    The name of the resource to retrieve.
     * @return The input stream for the requested resource.
     */
    @SneakyThrows
    public static InputStream getClassLoadHelperResourceAsStream(ClassLoadHelper classLoadHelper, String resourceName) {
        InputStream stream = classLoadHelper.getResourceAsStream(resourceName);
        if (stream == null) {
            throw new FileNotFoundException("Cannot read from File or Resource: '" + resourceName + "'");
        }
        return stream;
    }
}
