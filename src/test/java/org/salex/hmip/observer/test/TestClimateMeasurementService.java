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

import java.util.Date;
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
        homematicClient = mock(HmIPClient.class);
    }

    @Test
    void should_return_measurement_data_only_all_configured_sensors_available_in_the_cloud() throws Exception {
        when(homematicClient.loadCurrentState()).thenReturn(Mono.just(createHmIPState("TestClimateMeasurementService/hmip-state-with-three-sensor-devices.json")));
        when(database.getSensors()).thenReturn(List.of(
                new Sensor(1L, "Testsensor 1", Sensor.Type.HmIP_STHO, "test-sgtin-1"),
                new Sensor(2L, "Testsensor 2", Sensor.Type.HmIP_STHO, "test-sgtin-2"),
                new Sensor(2L, "Testsensor 3", Sensor.Type.HmIP_STHO, "test-sgtin-3")));
        service = new HomematicClimateMeasurementService(homematicClient, database);
        var reading = new Reading();
        StepVerifier
                .create(service.measureClimateValues(reading))
                .expectNext(reading)
                .verifyComplete();
        assertThat(reading.getMeasurements().size()).isEqualTo(3);
    }

    @Test
    void should_return_measurement_data_only_for_configured_sensors() throws Exception {
        when(homematicClient.loadCurrentState()).thenReturn(Mono.just(createHmIPState("TestClimateMeasurementService/hmip-state-with-three-sensor-devices.json")));
        when(database.getSensors()).thenReturn(List.of(
                new Sensor(1L, "Testsensor 1", Sensor.Type.HmIP_STHO, "test-sgtin-1"),
                new Sensor(2L, "Testsensor 2", Sensor.Type.HmIP_STHO, "test-sgtin-2")));
        service = new HomematicClimateMeasurementService(homematicClient, database);
        var reading = new Reading();
        StepVerifier
                .create(service.measureClimateValues(reading))
                .expectNext(reading)
                .verifyComplete();
        assertThat(reading.getMeasurements().size()).isEqualTo(2);
    }

    @Test
    void should_return_measurement_data_only_for_sensors_devices_available_in_the_cloud() throws Exception {
        when(homematicClient.loadCurrentState()).thenReturn(Mono.just(createHmIPState("TestClimateMeasurementService/hmip-state-with-two-sensor-devices.json")));
        when(database.getSensors()).thenReturn(List.of(
                new Sensor(1L, "Testsensor 1", Sensor.Type.HmIP_STHO, "test-sgtin-1"),
                new Sensor(2L, "Testsensor 2", Sensor.Type.HmIP_STHO, "test-sgtin-2"),
                new Sensor(2L, "Testsensor 3", Sensor.Type.HmIP_STHO, "test-sgtin-3")));
        service = new HomematicClimateMeasurementService(homematicClient, database);
        var reading = new Reading();
        StepVerifier
                .create(service.measureClimateValues(reading))
                .expectNext(reading)
                .verifyComplete();
        assertThat(reading.getMeasurements().size()).isEqualTo(2);
    }

    @Test
    void should_return_measurement_data_when_reading_sensor_device() throws Exception {
        when(homematicClient.loadCurrentState()).thenReturn(Mono.just(createHmIPState("TestClimateMeasurementService/hmip-state-with-one-sensor-device.json")));
        when(database.getSensors()).thenReturn(List.of(new Sensor(1L, "Testsensor 1", Sensor.Type.HmIP_STHO, "test-sgtin-1")));
        service = new HomematicClimateMeasurementService(homematicClient, database);
        var reading = new Reading();
        StepVerifier
                .create(service.measureClimateValues(reading))
                .expectNext(reading)
                .verifyComplete();
        assertThat(reading.getMeasurements().size()).isEqualTo(1);
        assertThat(reading.getMeasurements().get(0)).isInstanceOf(ClimateMeasurement.class);
        assertThat(((ClimateMeasurement)reading.getMeasurements().get(0)).getTemperature()).isEqualTo(7.3);
        assertThat(((ClimateMeasurement)reading.getMeasurements().get(0)).getHumidity()).isEqualTo(73);
        assertThat(((ClimateMeasurement)reading.getMeasurements().get(0)).getVaporAmount()).isEqualTo(7.348975847536114);
        assertThat(((ClimateMeasurement)reading.getMeasurements().get(0)).getMeasuringTime()).isEqualTo(new Date(1648411198375L));
    }

    private HmIPState createHmIPState(String resource) throws Exception {
        final var mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(new ClassPathResource(resource).getFile(), HmIPState.class);
    }
}
