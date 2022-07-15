package org.salex.hmip.observer.test;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salex.hmip.observer.data.ClimateMeasurement;
import org.salex.hmip.observer.data.ClimateMeasurementBoundaries;
import org.salex.hmip.observer.data.Reading;
import org.salex.hmip.observer.data.Sensor;
import org.salex.hmip.observer.service.ChartGenerator;
import org.salex.hmip.observer.service.JFreeChartGenerator;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TestChartGenerator {
    private ChartGenerator generator;

    @BeforeEach
    void setup() {
        this.generator = new JFreeChartGenerator();
    }

    @Test
    void should_generate_24_hour_chart_for_duplicate_measurements() throws IOException {
        final var now = new Date();
        final var tenMinutesAgo = new Date(now.getTime() - TimeUnit.MINUTES.toMillis(10));
        final var twentyMinutesAgo = new Date(now.getTime() - TimeUnit.MINUTES.toMillis(20));
        final var firstReading = new Reading(tenMinutesAgo);
        final var secondReading = new Reading(now);
        final var sensor = new Sensor(1L, "First", Sensor.Type.HmIP_STHO, "test-sgtin-1", "#FF0000");
        final var data = Map.of(
                sensor, (List<ClimateMeasurement>) new ArrayList<ClimateMeasurement>(List.of(
                        new ClimateMeasurement(firstReading, sensor, tenMinutesAgo, 11.2, 52.7, 5.2386758493768),
                        new ClimateMeasurement(secondReading, sensor, tenMinutesAgo, 11.2, 52.7, 5.2386758493768)
                ))
        );
        StepVerifier
                .create(generator.create24HourChart(twentyMinutesAgo, now, data))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void should_generate_24_hour_chart_for_measurements_per_sensor() throws IOException {
        final var now = new Date();
        final var tenMinutesAgo = new Date(now.getTime() - TimeUnit.MINUTES.toMillis(10));
        final var twentyMinutesAgo = new Date(now.getTime() - TimeUnit.MINUTES.toMillis(20));
        final var reading = new Reading(now);
        final var firstSensor = new Sensor(1L, "First", Sensor.Type.HmIP_STHO, "test-sgtin-1", "#FF0000");
        final var secondSensor = new Sensor(2L, "Second", Sensor.Type.HmIP_STHO, "test-sgtin-2", "#00FF00");
        final var data = Map.of(
                firstSensor, (List<ClimateMeasurement>) new ArrayList<ClimateMeasurement>(List.of(
                        new ClimateMeasurement(reading, firstSensor, twentyMinutesAgo, 11.2, 52.7, 5.2386758493768),
                        new ClimateMeasurement(reading, firstSensor, tenMinutesAgo, 13.2, 42.7, 5.2386758493768),
                        new ClimateMeasurement(reading, firstSensor, now, 12.2, 32.7, 5.2386758493768)
                )),
                secondSensor, (List<ClimateMeasurement>) new ArrayList<ClimateMeasurement>(List.of(
                        new ClimateMeasurement(reading, secondSensor, twentyMinutesAgo, 21.2, 82.7, 5.2386758493768),
                        new ClimateMeasurement(reading, secondSensor, tenMinutesAgo, 23.2, 72.7, 5.2386758493768),
                        new ClimateMeasurement(reading, secondSensor, now, 22.2, 62.7, 5.2386758493768)
                ))
        );
        StepVerifier
                .create(generator.create24HourChart(twentyMinutesAgo, now, data))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void should_generate_356_day_chart_for_temperature() throws IOException {
        final var now = new Date();
        final var yesterday = new Date(now.getTime() - TimeUnit.DAYS.toMillis(1));
        final var moreThanAYearAgo = new Date(now.getTime() - TimeUnit.DAYS.toMillis(370));
        final var firstSensor = new Sensor(1L, "First", Sensor.Type.HmIP_STHO, "test-sgtin-1", "#FF0000");
        final var secondSensor = new Sensor(2L, "Second", Sensor.Type.HmIP_STHO, "test-sgtin-2", "#00FF00");
        final var data = Map.of(
                firstSensor, (List<ClimateMeasurementBoundaries>) new ArrayList<ClimateMeasurementBoundaries>(List.of(
                        createBoundaries(firstSensor, now, 10.0, 15.0, 42.0, 56.0, 3.123, 5.321),
                        createBoundaries(firstSensor, yesterday, 12.0, 17.0, 47.0, 58.0, 4.123, 6.321),
                        createBoundaries(firstSensor, moreThanAYearAgo, 8.0, 13.0, 38.0, 49.0, 2.123, 4.321)
                )),
                secondSensor, (List<ClimateMeasurementBoundaries>) new ArrayList<ClimateMeasurementBoundaries>(List.of(
                        createBoundaries(secondSensor, now, 10.5, 15.5, 42.5, 56.5, 3.123, 5.321),
                        createBoundaries(secondSensor, yesterday, 12.5, 17.5, 47.5, 58.5, 4.123, 6.321),
                        createBoundaries(secondSensor, moreThanAYearAgo, 8.5, 13.5, 38.5, 49.5, 2.123, 4.321)
                )));
        StepVerifier
                .create(generator.create365DayTemperatureChart(yesterday, now, data.get(firstSensor), firstSensor))
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier
                .create(generator.create365DayTemperatureChart(yesterday, now, data.get(secondSensor), secondSensor))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void should_generate_356_day_chart_for_humidity() throws IOException {
        final var now = new Date();
        final var yesterday = new Date(now.getTime() - TimeUnit.DAYS.toMillis(1));
        final var moreThanAYearAgo = new Date(now.getTime() - TimeUnit.DAYS.toMillis(370));
        final var firstSensor = new Sensor(1L, "First", Sensor.Type.HmIP_STHO, "test-sgtin-1", "#FF0000");
        final var secondSensor = new Sensor(2L, "Second", Sensor.Type.HmIP_STHO, "test-sgtin-2", "#00FF00");
        final var data = Map.of(
                firstSensor, (List<ClimateMeasurementBoundaries>) new ArrayList<ClimateMeasurementBoundaries>(List.of(
                        createBoundaries(firstSensor, now, 10.0, 15.0, 42.0, 56.0, 3.123, 5.321),
                        createBoundaries(firstSensor, yesterday, 12.0, 17.0, 47.0, 58.0, 4.123, 6.321),
                        createBoundaries(firstSensor, moreThanAYearAgo, 8.0, 13.0, 38.0, 49.0, 2.123, 4.321)
                )),
                secondSensor, (List<ClimateMeasurementBoundaries>) new ArrayList<ClimateMeasurementBoundaries>(List.of(
                        createBoundaries(secondSensor, now, 10.5, 15.5, 42.5, 56.5, 3.123, 5.321),
                        createBoundaries(secondSensor, yesterday, 12.5, 17.5, 47.5, 58.5, 4.123, 6.321),
                        createBoundaries(secondSensor, moreThanAYearAgo, 8.5, 13.5, 38.5, 49.5, 2.123, 4.321)
                )));
        StepVerifier
                .create(generator.create365DayHumidityChart(yesterday, now, data.get(firstSensor), firstSensor))
                .expectNextCount(1)
                .verifyComplete();
        StepVerifier
                .create(generator.create365DayHumidityChart(yesterday, now, data.get(secondSensor), secondSensor))
                .expectNextCount(1)
                .verifyComplete();
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
