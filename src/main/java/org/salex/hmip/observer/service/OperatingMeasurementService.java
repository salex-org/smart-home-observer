package org.salex.hmip.observer.service;

import org.salex.hmip.observer.data.Reading;
import reactor.core.publisher.Mono;

public interface OperatingMeasurementService {
    Mono<Reading> measureOperatingValues(Reading reading);
}
