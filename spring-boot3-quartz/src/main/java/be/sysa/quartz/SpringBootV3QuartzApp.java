package be.sysa.quartz;

import org.springframework.boot.Banner.Mode;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableScheduling
@SpringBootApplication
@EnableTransactionManagement
public class SpringBootV3QuartzApp {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SpringBootV3QuartzApp.class).bannerMode(Mode.OFF).run(args);
    }

}
