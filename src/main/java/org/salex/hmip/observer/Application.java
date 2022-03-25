package org.salex.hmip.observer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.ApplicationPidFileWriter;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.salex.hmip.client.HmIPClient;
import org.salex.hmip.client.HmIPConfiguration;
import org.salex.hmip.client.HmIPProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ConfigurationPropertiesScan(basePackageClasses = HmIPProperties.class)
public class Application {
    public static void main(String[] args) {
        var app = new SpringApplication(Application.class);
        app.addListeners(new ApplicationPidFileWriter());
        app.run(args);
    }

    @Bean
    HmIPClient createHomematicClient(HmIPProperties properties) {
        return HmIPConfiguration.builder()
                .properties(properties)
                .build()
                .map(HmIPClient::new)
                .block();
    }
}
