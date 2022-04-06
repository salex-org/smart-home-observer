package org.salex.hmip.observer;

import org.salex.hmip.observer.service.OperatingAlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.salex.hmip.client.HmIPProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import reactor.core.publisher.Hooks;

import javax.annotation.PostConstruct;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan(basePackageClasses = HmIPProperties.class)
public class ObserverApplication {
    @Autowired
    private OperatingAlertService operatingAlertService;

    public static void main(String[] args) {
        System.setProperty("derby.stream.error.file", "logs/derby.log");
        var app = new SpringApplication(ObserverApplication.class);
        app.addListeners(new ApplicationPidFileWriter());
        app.run(args);
    }

    @PostConstruct
    void initializeErrorHandling() {
        Hooks.onErrorDropped(operatingAlertService::signal);
    }
}
