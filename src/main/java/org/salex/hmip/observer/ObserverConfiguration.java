package org.salex.hmip.observer;

import org.salex.hmip.client.HmIPClient;
import org.salex.hmip.client.HmIPConfiguration;
import org.salex.hmip.client.HmIPProperties;
import org.salex.hmip.observer.data.*;
import org.salex.hmip.observer.service.ClimateMeasurementService;
import org.salex.hmip.observer.service.HomematicClimateMeasurementService;
import org.salex.hmip.observer.service.OperatingMeasurementService;
import org.salex.hmip.observer.service.RaspberryOperatingMeasurementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class ObserverConfiguration {
    private static final Logger LOG = LoggerFactory.getLogger(ObserverConfiguration.class);

    @Bean
    HmIPClient createHomematicClient(HmIPProperties properties) {
        return HmIPConfiguration.builder()
                .properties(properties)
                .build()
                .map(HmIPClient::new)
                .block();
    }

    @Bean
    ObserverDatabase createDatabase(SensorRepository sensorRepository, ReadingRepository readingRepository, ClimateMeasurementRepository climateMeasurementRepository) {
        return new JpaObserverDatabase(sensorRepository, readingRepository, climateMeasurementRepository);
    }

    @Bean
    ClimateMeasurementService createClimateMeasurementService(HmIPClient client, ObserverDatabase database) {
        return new HomematicClimateMeasurementService(client, database);
    }

    @Bean
    @ConditionalOnProperty("org.salex.raspberry.script.cpu")
    OperatingMeasurementService createRaspberryOperatingMeasurementService(@Value("${org.salex.raspberry.script.cpu}") String cpuMeasureScript) {
        return new RaspberryOperatingMeasurementService(cpuMeasureScript);
    }

    @Bean
    @ConditionalOnMissingBean
    OperatingMeasurementService createNoOperatingMeasurementService() {
        LOG.warn("No service for measurement of operating values available, measuring will be skipped!");
        return Mono::just;
    }
}
