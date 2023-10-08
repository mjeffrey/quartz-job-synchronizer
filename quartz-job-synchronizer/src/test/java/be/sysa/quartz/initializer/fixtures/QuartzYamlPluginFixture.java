package be.sysa.quartz.initializer.fixtures;

import be.sysa.quartz.initializer.YamlJobSynchronizerPlugin;
import lombok.Getter;
import org.mockito.Mockito;
import org.quartz.Scheduler;
import org.quartz.simpl.SimpleClassLoadHelper;
import org.quartz.spi.ClassLoadHelper;

public class QuartzYamlPluginFixture {
    @Getter
    private Scheduler scheduler;
    private ClassLoadHelper classLoadHelper;
    @Getter
    private YamlJobSynchronizerPlugin plugin;

    public YamlJobSynchronizerPlugin start(){
        plugin.start();
        return plugin;
    }
    private QuartzYamlPluginFixture(String... files) {
        this.scheduler = Mockito.mock(Scheduler.class);
        this.classLoadHelper = new SimpleClassLoadHelper();
        plugin = new YamlJobSynchronizerPlugin();
        plugin.setFileNames(String.join(",",files));
        plugin.initialize("test", scheduler, classLoadHelper);
    }

    public static QuartzYamlPluginFixture init(String... files) {
        return new QuartzYamlPluginFixture(files);
    }
}
