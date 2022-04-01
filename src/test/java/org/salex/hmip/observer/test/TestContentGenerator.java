package org.salex.hmip.observer.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salex.hmip.observer.blog.Image;
import org.salex.hmip.observer.data.ClimateMeasurement;
import org.salex.hmip.observer.data.ClimateMeasurementBoundaries;
import org.salex.hmip.observer.data.Reading;
import org.salex.hmip.observer.data.Sensor;
import org.salex.hmip.observer.service.ContentGenerator;
import org.salex.hmip.observer.service.FreeMarkerContentGenerator;
import org.springframework.web.reactive.result.view.freemarker.FreeMarkerConfigurer;
import reactor.test.StepVerifier;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class TestContentGenerator {

    private ContentGenerator generator;

    @BeforeEach
    void setup() throws Exception {
        final var freeMarkerConfigurer = new FreeMarkerConfigurer();
        freeMarkerConfigurer.setTemplateLoaderPath("classpath:/templates");
        freeMarkerConfigurer.setDefaultEncoding("UTF-8");
        freeMarkerConfigurer.setConfiguration(freeMarkerConfigurer.createConfiguration());
        this.generator = new FreeMarkerContentGenerator(freeMarkerConfigurer);
    }

    @Test
    void should_generate_overview_content_when_called_with_correct_reading() {
        final var now = new Date();
        final var reading = new Reading(now);
        final var firstSensor = new Sensor(1L, "First", Sensor.Type.HmIP_STHO, "test-sgtin-1", "#FF0000");
        final var secondSensor = new Sensor(2L, "Second", Sensor.Type.HmIP_STHO, "test-sgtin-2", "#00FF00");
        reading.addMeasurement(new ClimateMeasurement(reading, firstSensor, now, 13.2, 42.7, 5.2386758493768));
        reading.addMeasurement(new ClimateMeasurement(reading, secondSensor, now, 16.8, 38.5, 6.6784739686878));
        StepVerifier
                .create(generator.generateOverview(reading))
                .expectNextCount(1)
                .verifyComplete();
        assertThat(reading.getMeasurements().size()).isEqualTo(2);
    }

    @Test
    void should_return_just_header_when_generate_details_is_called_with_empty_data() {
        final var image = new Image("Ei-Die");
        final var now = new Date();
        final var oneDayAgo = new Date(now.getTime() - TimeUnit.HOURS.toMillis(24));
        final var data = new HashMap<Sensor, List<ClimateMeasurement>>();
        StepVerifier
                .create(generator.generateDetails(oneDayAgo, now, data, image))
                .expectNext("<h3>Keine Daten vorhanden</h3>")
                .verifyComplete();
    }

    @Test
    void should_generate_details_content_when_called_with_correct_data() {
        final var now = new Date();
        final var tenMinutesAgo = new Date(now.getTime() - TimeUnit.MINUTES.toMillis(10));
        final var twentyMinutesAgo = new Date(now.getTime() - TimeUnit.MINUTES.toMillis(20));
        final var reading = new Reading(now);
        final var image = createImage("test-image","http://link-to-test-image");
        final var firstSensor = new Sensor(1L, "First", Sensor.Type.HmIP_STHO, "test-sgtin-1", "#FF0000");
        final var secondSensor = new Sensor(2L, "Second", Sensor.Type.HmIP_STHO, "test-sgtin-2", "#00FF00");
        final var data = Map.of(
                firstSensor, List.of(
                        new ClimateMeasurement(reading, firstSensor, twentyMinutesAgo, 11.2, 52.7, 5.2386758493768),
                        new ClimateMeasurement(reading, firstSensor, tenMinutesAgo, 13.2, 42.7, 5.2386758493768),
                        new ClimateMeasurement(reading, firstSensor, now, 12.2, 32.7, 5.2386758493768)
                ),
                secondSensor, List.of(
                        new ClimateMeasurement(reading, secondSensor, twentyMinutesAgo, 21.2, 82.7, 5.2386758493768),
                        new ClimateMeasurement(reading, secondSensor, tenMinutesAgo, 23.2, 72.7, 5.2386758493768),
                        new ClimateMeasurement(reading, secondSensor, now, 22.2, 62.7, 5.2386758493768)
                )
        );
       StepVerifier
                .create(generator.generateDetails(twentyMinutesAgo, now, data, image))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void should_return_just_header_when_generate_history_is_called_with_empty_data() {
        final var now = new Date();
        final var oneDayAgo = new Date(now.getTime() - TimeUnit.HOURS.toMillis(24));
        final var firstSensor = new Sensor(1L, "First", Sensor.Type.HmIP_STHO, "test-sgtin-1", "#FF0000");
        final var data = new HashMap<Sensor, List<ClimateMeasurementBoundaries>>();
        StepVerifier
                .create(generator.generateHistory(oneDayAgo, now, data, Map.of(firstSensor, new HashMap<String, Image>())))
                .expectNext("<h3>Keine Daten vorhanden</h3>")
                .verifyComplete();
    }

    @Test
    void should_return_just_header_when_generate_history_is_called_with_empty_diagrams() {
        final var now = new Date();
        final var yesterday = new Date(now.getTime() - TimeUnit.DAYS.toMillis(1));
        final var firstSensor = new Sensor(1L, "First", Sensor.Type.HmIP_STHO, "test-sgtin-1", "#FF0000");
        final var data = new HashMap<Sensor, List<ClimateMeasurementBoundaries>>();
        data.put(firstSensor, new ArrayList<>());
        StepVerifier
                .create(generator.generateHistory(yesterday, now, data, Map.of()))
                .expectNext("<h3>Keine Diagramme vorhanden</h3>")
                .verifyComplete();
    }

    @Test
    void should_generate_history_content_when_called_with_correct_data() {
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
        final var images = Map.of(
                firstSensor, Map.of(
                        "temperature", createImage("temp-chart-1","http://link-to-temp-chart-1", "http://link-to-temp-thumbnail-1"),
                        "humidity", createImage("hum-chart-1","http://link-to-hum-chart-1", "http://link-to-hum-thumbnail-1")
                ),
                secondSensor, Map.of(
                        "temperature", createImage("temp-chart-2","http://link-to-temp-chart-2", "http://link-to-temp-thumbnail-2"),
                        "humidity", createImage("hum-chart-2","http://link-to-hum-chart-2", "http://link-to-hum-thumbnail-2")
                )
        );
        StepVerifier
                .create(generator.generateHistory(yesterday, now, data, images))
                .expectNextCount(1)
                .verifyComplete();
    }

    private Image createImage(String id, String full) {
        return createImage(id, full, null);
    }

    private Image createImage(String id, String full, String thumbnail) {
        final var image = new Image(id);
        image.setFull(full);
        image.setThumbnail(thumbnail);
        image.setThumbnailHeight(600);
        image.setThumbnailWidth(800);
        return image;
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
