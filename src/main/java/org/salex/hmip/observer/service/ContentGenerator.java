package org.salex.hmip.observer.service;

import org.salex.hmip.observer.blog.Image;
import org.salex.hmip.observer.data.ClimateMeasurement;
import org.salex.hmip.observer.data.Reading;
import org.salex.hmip.observer.data.Sensor;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

public interface ContentGenerator {
    Mono<String> generateOverview(Reading reading);

    Mono<String> generateDetails(Map<Sensor, List<ClimateMeasurement>> data, Image diagram);
}
