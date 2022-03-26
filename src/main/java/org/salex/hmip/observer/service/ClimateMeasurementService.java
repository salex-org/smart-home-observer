package org.salex.hmip.observer.service;

import org.salex.hmip.observer.data.Reading;
import reactor.core.publisher.Mono;

public interface ClimateMeasurementService {
    Mono<Reading> measureClimateValues(Reading reading);
}
