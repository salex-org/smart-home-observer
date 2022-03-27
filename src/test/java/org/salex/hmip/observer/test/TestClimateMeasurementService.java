package org.salex.hmip.observer.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salex.hmip.client.HmIPClient;
import org.salex.hmip.client.HmIPState;
import org.salex.hmip.observer.data.ClimateMeasurement;
import org.salex.hmip.observer.data.ObserverDatabase;
import org.salex.hmip.observer.data.Reading;
import org.salex.hmip.observer.data.Sensor;
import org.salex.hmip.observer.service.ClimateMeasurementService;
import org.salex.hmip.observer.service.HomematicClimateMeasurementService;
import org.springframework.core.io.ClassPathResource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class TestClimateMeasurementService {
    private ObserverDatabase database;
    private HmIPClient homematicClient;
    private ClimateMeasurementService service;

    @BeforeEach
    void setup() {
        database = mock(ObserverDatabase.class);
        when(database.getSensors()).thenReturn(List.of(
                new Sensor(1L, "Testsensor 1", Sensor.Type.HmIP_STHO, "test-sgtin-1"),
                new Sensor(2L, "Testsensor 2", Sensor.Type.HmIP_STHO, "test-sgtin-2")));
        homematicClient = mock(HmIPClient.class);
        service = new HomematicClimateMeasurementService(homematicClient, database);
    }

    @Test
    void testReadClimateDataFromTheCloud() throws Exception {
        when(homematicClient.loadCurrentState()).thenReturn(Mono.just(createHmIPState("TestClimateMeasurementService/testReadClimateDataFromTheCLoud.json")));
        var reading = new Reading();
        StepVerifier
                .create(service.measureClimateValues(reading))
                .expectNext(reading)
                .verifyComplete();
        assertThat(reading.getMeasurements().size()).isEqualTo(2);
    }

    @Test
    void testReadClimateDataMissingInTheCloud() throws Exception {
        when(homematicClient.loadCurrentState()).thenReturn(Mono.just(createHmIPState("TestClimateMeasurementService/testReadClimateDataMissingInTheCloud.json")));
        var reading = new Reading();
        StepVerifier
                .create(service.measureClimateValues(reading))
                .expectNext(reading)
                .verifyComplete();
        assertThat(reading.getMeasurements().size()).isEqualTo(1);
        assertThat(reading.getMeasurements().get(0)).isInstanceOf(ClimateMeasurement.class);
        assertThat(((ClimateMeasurement)reading.getMeasurements().get(0)).getTemperature()).isEqualTo(7.3);
        assertThat(((ClimateMeasurement)reading.getMeasurements().get(0)).getHumidity()).isEqualTo(73);
    }

    private HmIPState createHmIPState(String resource) throws Exception {
        final var mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(new ClassPathResource(resource).getFile(), HmIPState.class);
    }
}
