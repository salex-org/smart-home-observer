package org.salex.hmip.observer;

import org.salex.hmip.client.HmIPClient;
import org.salex.hmip.client.HmIPConfiguration;
import org.salex.hmip.client.HmIPProperties;
import org.salex.hmip.observer.data.*;
import org.salex.hmip.observer.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

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

    @Bean
    @ConditionalOnProperty("org.salex.blog.url")
    BlogPublishService createWordPressPublishService(
            @Value("${org.salex.blog.url}") String url,
            @Value("${org.salex.blog.username}") String username,
            @Value("${org.salex.blog.password}") String password) {
        final var basicAuth = HttpHeaders.encodeBasicAuth(username, password, null);
        final var client = WebClient.builder().baseUrl(url).defaultHeaders(headers -> {
            headers.setBasicAuth(basicAuth);
            headers.setContentType(MediaType.APPLICATION_JSON);
        }).build();
        return new WordPressPublishService(client);
    }

    @Bean
    @ConditionalOnMissingBean
    BlogPublishService createNoOperatingPublishService() {
        LOG.warn("No service for blog publishing available, publishing climate information will be skipped!");
        return new BlogPublishService() {
            @Override
            public Mono<Reading> postOverview(Reading reading) {
                return Mono.just(reading);
            }

            @Override
            public Mono<Void> postDetails(List<ClimateMeasurement> measurements) {
                return Mono.empty();
            }
        };
    }

}
