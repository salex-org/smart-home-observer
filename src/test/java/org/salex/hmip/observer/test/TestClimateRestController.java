package org.salex.hmip.observer.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salex.hmip.observer.controller.ClimateRestController;
import org.salex.hmip.observer.data.ClimateMeasurement;
import org.salex.hmip.observer.data.ObserverDatabase;
import org.salex.hmip.observer.data.Reading;
import org.salex.hmip.observer.data.Sensor;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestClimateRestController {
    private ObserverDatabase database;

    @BeforeEach
    void setup() {
        database = mock(ObserverDatabase.class);
    }

    @Test
    void should_return_measurement_data_of_the_past_two_hours() {
        final var now = new Date();
        final var reading = new Reading((now));
        final var firstSensor = new Sensor(1L, "Testsensor 1", Sensor.Type.HmIP_STHO, "test-sgtin-1", "#FF0000");
        final var secondSensor = new Sensor(2L, "Testsensor 2", Sensor.Type.HmIP_STHO, "test-sgtin-2", "#00FF00");
        final var oneHourAgo = new Date(now.getTime() - TimeUnit.HOURS.toMillis(1));
        final var threeHoursAgo = new Date(now.getTime() - TimeUnit.HOURS.toMillis(3));
        when(database.getClimateMeasurements(2)).thenReturn(Map.of(
                firstSensor, List.of(
                    new ClimateMeasurement(reading, firstSensor, now, 12.3, 42.7, 3.45674395764),
                    new ClimateMeasurement(reading, firstSensor, oneHourAgo, 11.2, 46.2, 4.46836586586)
                ),
                secondSensor, List.of(
                        new ClimateMeasurement(reading, secondSensor, now, 15.7, 43.5, 3.14159265358),
                        new ClimateMeasurement(reading, secondSensor, oneHourAgo, 14.4, 45.9, 4.87674638485)
                )));
        final var controller = new ClimateRestController(database);
        final var result = controller.getPastClimateMeasurements(2);
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.get(firstSensor).size()).isEqualTo(2);
        assertThat(result.get(secondSensor).size()).isEqualTo(2);
    }

}
