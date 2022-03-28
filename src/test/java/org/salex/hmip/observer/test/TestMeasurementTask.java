package org.salex.hmip.observer.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salex.hmip.observer.data.*;
import org.salex.hmip.observer.service.ClimateMeasurementService;
import org.salex.hmip.observer.service.OperatingMeasurementService;
import org.salex.hmip.observer.task.MeasurementTask;
import reactor.core.publisher.Mono;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestMeasurementTask {
    private ObserverDatabase database;
    private ClimateMeasurementService climateMeasurementService;
    private OperatingMeasurementService operatingMeasurementService;

    @BeforeEach
    void setup() {
        database = mock(ObserverDatabase.class);
        climateMeasurementService = mock(ClimateMeasurementService.class);
        operatingMeasurementService = mock(OperatingMeasurementService.class);
    }

    @Test
    void should_read_latest_measurement_data_and_add_reading_to_database() {
        final var now = new Date();
        final var reading = new Reading(now);
        final var firstSensor = new Sensor(1L, "Testsensor 1", Sensor.Type.HmIP_STHO, "test-sgtin-1");
        final var secondSensor = new Sensor(2L, "Testsensor 2", Sensor.Type.HmIP_STHO, "test-sgtin-2");
        reading.addMeasurement(new ClimateMeasurement(reading, firstSensor, now, 12.3, 42.7, 3.45674395764));
        reading.addMeasurement(new ClimateMeasurement(reading, secondSensor, now, 15.7, 43.5, 3.14159265358));
        reading.addMeasurement(new OperatingMeasurement(reading, 1.0, 2.0, 3.0, 4.0));
        when(climateMeasurementService.measureClimateValues(any())).thenReturn(Mono.just(reading));
        when(operatingMeasurementService.measureOperatingValues(any())).thenReturn(Mono.just(reading));
        final var task = new MeasurementTask("test-cron", database, operatingMeasurementService, climateMeasurementService);
        task.measure();
        verify(climateMeasurementService, times(1)).measureClimateValues(any());
        verify(operatingMeasurementService, times(1)).measureOperatingValues(any());
        verify(database, times(1)).addReading(reading);
        verifyNoMoreInteractions(climateMeasurementService);
        verifyNoMoreInteractions(operatingMeasurementService);
        verifyNoMoreInteractions(database);
    }

    @Test
    void should_skip_add_reading_to_database_when_error_occurred_in_climate_measurement() {
        final var now = new Date();
        final var reading = new Reading(now);
        final var firstSensor = new Sensor(1L, "Testsensor 1", Sensor.Type.HmIP_STHO, "test-sgtin-1");
        final var secondSensor = new Sensor(2L, "Testsensor 2", Sensor.Type.HmIP_STHO, "test-sgtin-2");
        reading.addMeasurement(new ClimateMeasurement(reading, firstSensor, now, 12.3, 42.7, 3.45674395764));
        reading.addMeasurement(new ClimateMeasurement(reading, secondSensor, now, 15.7, 43.5, 3.14159265358));
        reading.addMeasurement(new OperatingMeasurement(reading, 1.0, 2.0, 3.0, 4.0));
        when(climateMeasurementService.measureClimateValues(any())).thenReturn(Mono.error(new Exception("test exception when reading climate measurements")));
        when(operatingMeasurementService.measureOperatingValues(any())).thenReturn(Mono.just(reading));
        final var task = new MeasurementTask("test-cron", database, operatingMeasurementService, climateMeasurementService);
        task.measure();
        verifyNoInteractions(database);
    }

    @Test
    void should_skip_add_reading_to_database_when_error_occurred_in_operating_measurement() {
        final var now = new Date();
        final var reading = new Reading(now);
        final var firstSensor = new Sensor(1L, "Testsensor 1", Sensor.Type.HmIP_STHO, "test-sgtin-1");
        final var secondSensor = new Sensor(2L, "Testsensor 2", Sensor.Type.HmIP_STHO, "test-sgtin-2");
        reading.addMeasurement(new ClimateMeasurement(reading, firstSensor, now, 12.3, 42.7, 3.45674395764));
        reading.addMeasurement(new ClimateMeasurement(reading, secondSensor, now, 15.7, 43.5, 3.14159265358));
        reading.addMeasurement(new OperatingMeasurement(reading, 1.0, 2.0, 3.0, 4.0));
        when(climateMeasurementService.measureClimateValues(any())).thenReturn(Mono.just(reading));
        when(operatingMeasurementService.measureOperatingValues(any())).thenReturn(Mono.error(new Exception("test exception when reading operating measurements")));
        final var task = new MeasurementTask("test-cron", database, operatingMeasurementService, climateMeasurementService);
        task.measure();
        verifyNoInteractions(database);
    }
}
