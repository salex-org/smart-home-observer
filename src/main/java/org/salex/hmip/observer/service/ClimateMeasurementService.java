package org.salex.hmip.observer.service;

import org.salex.hmip.observer.data.Reading;
import org.salex.hmip.observer.data.Sensor;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ClimateMeasurementService {
    Mono<Reading> measureClimateValues(Reading reading, List<Sensor> sensors);
}
