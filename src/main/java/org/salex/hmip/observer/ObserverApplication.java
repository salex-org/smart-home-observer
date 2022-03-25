package org.salex.hmip.observer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.salex.hmip.client.HmIPProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan(basePackageClasses = HmIPProperties.class)
public class ObserverApplication {
    public static void main(String[] args) {
        var app = new SpringApplication(ObserverApplication.class);
        app.addListeners(new ApplicationPidFileWriter());
        app.run(args);
    }
}
