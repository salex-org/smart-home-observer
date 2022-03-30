package org.salex.hmip.observer.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salex.hmip.observer.data.*;
import org.salex.hmip.observer.service.BlogPublishService;
import org.salex.hmip.observer.service.ClimateMeasurementService;
import org.salex.hmip.observer.service.OperatingMeasurementService;
import org.salex.hmip.observer.task.MeasurementTask;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestMeasurementTask {
    private ObserverDatabase database;
    private ClimateMeasurementService climateMeasurementService;
    private OperatingMeasurementService operatingMeasurementService;
    private BlogPublishService blogPublishService;

    @BeforeEach
    void setup() {
        database = mock(ObserverDatabase.class);
        climateMeasurementService = mock(ClimateMeasurementService.class);
        operatingMeasurementService = mock(OperatingMeasurementService.class);
        blogPublishService = mock(BlogPublishService.class);
    }

    @Test
    void should_read_latest_measurement_data_and_add_reading_to_database() {
        final var now = new Date();
        final var reading = new Reading(now);
        final var firstSensor = new Sensor(1L, "Testsensor 1", Sensor.Type.HmIP_STHO, "test-sgtin-1", "#FF0000");
        final var secondSensor = new Sensor(2L, "Testsensor 2", Sensor.Type.HmIP_STHO, "test-sgtin-2", "#00FF00");
        reading.addMeasurement(new ClimateMeasurement(reading, firstSensor, now, 12.3, 42.7, 3.45674395764));
        reading.addMeasurement(new ClimateMeasurement(reading, secondSensor, now, 15.7, 43.5, 3.14159265358));
        reading.addMeasurement(new OperatingMeasurement(reading, 1.0, 2.0, 3.0, 4.0));
        when(climateMeasurementService.measureClimateValues(any())).thenReturn(Mono.just(reading));
        when(operatingMeasurementService.measureOperatingValues(any())).thenReturn(Mono.just(reading));
        when(blogPublishService.postOverview(any())).thenReturn(Mono.just(reading));
        when(blogPublishService.postDetails(any(Date.class), any(Date.class), any())).thenReturn(Mono.just(new HashMap<Sensor, List<ClimateMeasurement>>()));
        when(database.addReading(any())).thenReturn(reading);
        when(database.getClimateMeasurements(any(Date.class), any(Date.class))).thenReturn(new HashMap<Sensor, List<ClimateMeasurement>>());
        final var task = new MeasurementTask("test-cron", database, operatingMeasurementService, climateMeasurementService, blogPublishService);
        task.measure();
        verify(climateMeasurementService, times(1)).measureClimateValues(any());
        verify(operatingMeasurementService, times(1)).measureOperatingValues(any());
        verify(database, times(1)).addReading(reading);
        verify(database, times(1)).getClimateMeasurements(any(Date.class), any(Date.class));
        verify(blogPublishService, times(1)).postOverview(reading);
        verify(blogPublishService, times(1)).postDetails(any(Date.class), any(Date.class), any());
        verifyNoMoreInteractions(climateMeasurementService);
        verifyNoMoreInteractions(operatingMeasurementService);
        verifyNoMoreInteractions(database);
        verifyNoMoreInteractions(blogPublishService);
    }

    @Test
    void should_skip_add_reading_to_database_when_error_occurred_in_climate_measurement() {
        final var now = new Date();
        final var reading = new Reading(now);
        final var firstSensor = new Sensor(1L, "Testsensor 1", Sensor.Type.HmIP_STHO, "test-sgtin-1", "#FF0000");
        final var secondSensor = new Sensor(2L, "Testsensor 2", Sensor.Type.HmIP_STHO, "test-sgtin-2", "#00FF00");
        reading.addMeasurement(new ClimateMeasurement(reading, firstSensor, now, 12.3, 42.7, 3.45674395764));
        reading.addMeasurement(new ClimateMeasurement(reading, secondSensor, now, 15.7, 43.5, 3.14159265358));
        reading.addMeasurement(new OperatingMeasurement(reading, 1.0, 2.0, 3.0, 4.0));
        when(climateMeasurementService.measureClimateValues(any())).thenReturn(Mono.error(new Exception("test exception when reading climate measurements")));
        when(operatingMeasurementService.measureOperatingValues(any())).thenReturn(Mono.just(reading));
        final var task = new MeasurementTask("test-cron", database, operatingMeasurementService, climateMeasurementService, blogPublishService);
        task.measure();
        verifyNoInteractions(database);
        verifyNoInteractions(blogPublishService);
    }

    @Test
    void should_skip_add_reading_to_database_when_error_occurred_in_operating_measurement() {
        final var now = new Date();
        final var reading = new Reading(now);
        final var firstSensor = new Sensor(1L, "Testsensor 1", Sensor.Type.HmIP_STHO, "test-sgtin-1", "#FF0000");
        final var secondSensor = new Sensor(2L, "Testsensor 2", Sensor.Type.HmIP_STHO, "test-sgtin-2", "#00FF00");
        reading.addMeasurement(new ClimateMeasurement(reading, firstSensor, now, 12.3, 42.7, 3.45674395764));
        reading.addMeasurement(new ClimateMeasurement(reading, secondSensor, now, 15.7, 43.5, 3.14159265358));
        reading.addMeasurement(new OperatingMeasurement(reading, 1.0, 2.0, 3.0, 4.0));
        when(climateMeasurementService.measureClimateValues(any())).thenReturn(Mono.just(reading));
        when(operatingMeasurementService.measureOperatingValues(any())).thenReturn(Mono.error(new Exception("test exception when reading operating measurements")));
        final var task = new MeasurementTask("test-cron", database, operatingMeasurementService, climateMeasurementService, blogPublishService);
        task.measure();
        verifyNoInteractions(database);
        verifyNoInteractions(blogPublishService);
    }
}
