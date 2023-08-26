package be.sysa.quartz.initializer;

import be.sysa.quartz.SpringBoot3QuartzApp;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {SpringBoot3QuartzApp.class})
@Slf4j
public abstract class IntegrationTest {

    @LocalServerPort
    protected int port;

    @Autowired
    Scheduler scheduler;

}
