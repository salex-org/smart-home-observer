package org.salex.hmip.observer.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salex.hmip.observer.blog.Image;
import org.salex.hmip.observer.data.ClimateMeasurement;
import org.salex.hmip.observer.data.Reading;
import org.salex.hmip.observer.data.Sensor;
import org.salex.hmip.observer.service.ContentGenerator;
import org.salex.hmip.observer.service.FreeMarkerContentGenerator;
import org.springframework.web.reactive.result.view.freemarker.FreeMarkerConfigurer;
import reactor.test.StepVerifier;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    void should_generate_details_content_when_called_with_correct_reading() {
        final var now = new Date();
        final var tenMinutesAgo = new Date(now.getTime() - TimeUnit.MINUTES.toMillis(10));
        final var twentyMinutesAgo = new Date(now.getTime() - TimeUnit.MINUTES.toMillis(20));
        final var reading = new Reading(now);
        final var image = new Image("test-image");
        image.setFull("http://link-to-test-image");
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

        generator.generateDetails(twentyMinutesAgo, now, data, image).subscribe(content -> System.out.println(content));
       /* StepVerifier
                .create(generator.generateDetails(data, image))
                .expectNextCount(1)
                .verifyComplete(); */
        // TODO implement
    }
}
