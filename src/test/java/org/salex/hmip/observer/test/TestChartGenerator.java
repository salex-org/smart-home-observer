package org.salex.hmip.observer.test;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salex.hmip.observer.data.ClimateMeasurement;
import org.salex.hmip.observer.data.Reading;
import org.salex.hmip.observer.data.Sensor;
import org.salex.hmip.observer.service.ChartGenerator;
import org.salex.hmip.observer.service.JFreeChartGenerator;

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
        final var png = generator.create24HourChart(twentyMinutesAgo, now, data);
        Assertions.assertThat(png).isNotEmpty();
    }

    @Test
    void should_generate_356_day_chart_for_temperature() throws IOException {
        // TODO implement
    }

    @Test
    void should_generate_356_day_chart_for_humidity() throws IOException {
        // TODO implement
    }
}
