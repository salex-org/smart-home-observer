package org.salex.hmip.observer;

import org.salex.hmip.observer.task.MeasurementTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.salex.hmip.client.HmIPProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import reactor.core.publisher.Hooks;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan(basePackageClasses = HmIPProperties.class)
public class ObserverApplication {
    private static final Logger LOG = LoggerFactory.getLogger(ObserverApplication.class);

    public static void main(String[] args) {
        // Don't mess the log with stack traces
        Hooks.onErrorDropped(error -> LOG.debug(error.getMessage(), error));
        var app = new SpringApplication(ObserverApplication.class);
        app.addListeners(new ApplicationPidFileWriter());
        app.run(args);
    }
}
