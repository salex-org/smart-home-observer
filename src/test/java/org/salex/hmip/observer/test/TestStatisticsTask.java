package org.salex.hmip.observer.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salex.hmip.observer.data.*;
import org.salex.hmip.observer.service.BlogPublishService;
import org.salex.hmip.observer.task.StatisticsTask;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class TestStatisticsTask {
    private ObserverDatabase database;
    private BlogPublishService blogPublishService;

    @BeforeEach
    void setup() {
        database = mock(ObserverDatabase.class);
        blogPublishService = mock(BlogPublishService.class);
    }

    @Test
    void should_publish_history_with_climate_statistics() {
        // Prepare the test data
        final var now = new Date();
        final var yesterday = new Date(now.getTime() - TimeUnit.DAYS.toMillis(1));
        final var moreThanAYearAgo = new Date(now.getTime() - TimeUnit.DAYS.toMillis(370));
        final var firstSensor = new Sensor(1L, "First", Sensor.Type.HmIP_STHO, "test-sgtin-1", "#FF0000");
        final var secondSensor = new Sensor(2L, "Second", Sensor.Type.HmIP_STHO, "test-sgtin-2", "#00FF00");
        final var data = Map.of(
                firstSensor, List.of(
                        createBoundaries(firstSensor, now, 10.0, 15.0, 42.0, 56.0, 3.123, 5.321),
                        createBoundaries(firstSensor, yesterday, 12.0, 17.0, 47.0, 58.0, 4.123, 6.321),
                        createBoundaries(firstSensor, moreThanAYearAgo, 8.0, 13.0, 38.0, 49.0, 2.123, 4.321)
                ),
                secondSensor, List.of(
                        createBoundaries(secondSensor, now, 10.5, 15.5, 42.5, 56.5, 3.123, 5.321),
                        createBoundaries(secondSensor, yesterday, 12.5, 17.5, 47.5, 58.5, 4.123, 6.321),
                        createBoundaries(secondSensor, moreThanAYearAgo, 8.5, 13.5, 38.5, 49.5, 2.123, 4.321)
                ));

        // Prepare the mocks
        when(database.getClimateMeasurementBoundaries(any(Date.class), any(Date.class))).thenReturn(data);
        when(blogPublishService.postHistory(any(Date.class), any(Date.class), any())).thenReturn(Mono.just(data));

        // Create and call the task
        final var task = new StatisticsTask("test-cron", database, blogPublishService);
        task.updateStatistics();

        // Verification
        verify(database, times(1)).getClimateMeasurementBoundaries(any(Date.class), any(Date.class));
        verify(blogPublishService, times(1)).postHistory(any(Date.class), any(Date.class), any());
        verifyNoMoreInteractions(database);
        verifyNoMoreInteractions(blogPublishService);
    }

    private ClimateMeasurementBoundaries createBoundaries(Sensor sensor, Date day, Double minTemp, Double maxTemp, Double minHum, Double maxHum, Double minVap, Double maxVap) {
        return new ClimateMeasurementBoundaries() {
            @Override
            public Double getMinimumTemperature() {
                return minTemp;
            }
            @Override
            public Double getMaximumTemperature() {
                return maxTemp;
            }
            @Override
            public Double getMinimumHumidity() {
                return minHum;
            }
            @Override
            public Double getMaximumHumidity() {
                return maxHum;
            }
            @Override
            public Double getMinimumVaporAmount() {
                return minVap;
            }
            @Override
            public Double getMaximumVaporAmount() {
                return maxVap;
            }
            @Override
            public Long getSensorId() {
                return sensor.getId();
            }
            @Override
            public Date getDay() {
                return day;
            }
        };
    }
}
