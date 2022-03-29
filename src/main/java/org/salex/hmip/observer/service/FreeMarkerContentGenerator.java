package org.salex.hmip.observer.service;

import freemarker.core.TemplateNumberFormatFactory;
import freemarker.template.Template;
import org.salex.hmip.observer.blog.Image;
import org.salex.hmip.observer.data.ClimateMeasurement;
import org.salex.hmip.observer.data.Reading;
import org.salex.hmip.observer.data.Sensor;
import org.springframework.web.reactive.result.view.freemarker.FreeMarkerConfigurer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

public class FreeMarkerContentGenerator implements ContentGenerator {
    private static final String TIMESTAMP_FORMAT = "dd.MM.yyyy HH:mm";
    private static final String DATE_FORMAT = "dd.MM.yyyy";
    private static final String TIME_FORMAT = "HH:mm";

    private final Template overviewTemplate;

    private final Template detailsTemplate;

    public FreeMarkerContentGenerator(FreeMarkerConfigurer freeMarkerConfigurer) throws Exception {
        final var customNumberFormats = new HashMap<String, TemplateNumberFormatFactory>();
        customNumberFormats.put("temp", FreeMarkerTemperatureFormat.factory());
        customNumberFormats.put("hum", FreeMarkerHumidityFormat.factory());

        this.overviewTemplate = freeMarkerConfigurer.getConfiguration().getTemplate("blog/wordpress/overview.ftl");
        this.overviewTemplate.setDateFormat(DATE_FORMAT);
        this.overviewTemplate.setTimeFormat(TIME_FORMAT);
        this.overviewTemplate.setCustomNumberFormats(customNumberFormats);

        this.detailsTemplate = freeMarkerConfigurer.getConfiguration().getTemplate("blog/wordpress/details.ftl");
        this.detailsTemplate.setDateTimeFormat(TIMESTAMP_FORMAT);
        this.detailsTemplate.setCustomNumberFormats(customNumberFormats);
    }

    @Override
    public Mono<String> generateOverview(Reading reading) {
        final var templateData = new HashMap<String, Object>();
        templateData.put("readingTime", reading.getReadingTime());
        templateData.put("measurements", reading.getMeasurements().stream()
                .filter(m -> m instanceof ClimateMeasurement)
                .map(ClimateMeasurement.class::cast)
                .collect(Collectors.toList())
        );
        try {
            final var content = new StringWriter();
            this.overviewTemplate.process(templateData, content);
            return Mono.just(content.toString());
        } catch(Exception e) {
            return Mono.error(e);
        }
    }

    @Override
    public Mono<String> generateDetails(Map<Sensor, List<ClimateMeasurement>> data, Image diagram) {
        if(data.isEmpty()) {
            return Mono.just("<h3>Keine Daten vorhanden</h3>");
        } else {
            return Flux.fromIterable(data.keySet())
                    .map(sensor -> {
                        final var templateData = new HashMap<String, Object>();
                        final var measurements = data.get(sensor);
                        templateData.put("minTemp", measurements.stream().min(Comparator.comparing(ClimateMeasurement::getTemperature)).orElseThrow());
                        templateData.put("maxTemp", measurements.stream().max(Comparator.comparing(ClimateMeasurement::getTemperature)).orElseThrow());
                        templateData.put("minHum", measurements.stream().min(Comparator.comparing(ClimateMeasurement::getHumidity)).orElseThrow());
                        templateData.put("maxHum", measurements.stream().max(Comparator.comparing(ClimateMeasurement::getHumidity)).orElseThrow());
                        templateData.put("sensor", sensor);
                        templateData.put("color", "#00AA00"); // TODO implement sensor specific color
                        return templateData;
                    })
                    .collectList()
                    .map(measurements -> {
                        final var templateData = new HashMap<String, Object>();
                        templateData.put("measurements", measurements);
                        templateData.put("periodStart", new Date()); // TODO implement
                        templateData.put("periodEnd", new Date()); // TODO implement
                        templateData.put("diagramId", "d-id"); // TODO implement
                        templateData.put("diagramFull", "d-full"); // TODO implement
                        return templateData;
                    })
                    .flatMap(templateData -> {
                        try {
                            final var content = new StringWriter();
                            this.detailsTemplate.process(templateData, content);
                            return Mono.just(content.toString());
                        } catch (Exception e) {
                            return Mono.error(e);
                        }
                    });

        }
    }
}
