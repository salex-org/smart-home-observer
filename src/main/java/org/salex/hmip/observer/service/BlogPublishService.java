package org.salex.hmip.observer.service;

import org.salex.hmip.observer.data.ClimateMeasurement;
import org.salex.hmip.observer.data.Reading;
import reactor.core.publisher.Mono;

import java.util.List;

public interface BlogPublishService {
    Mono<Reading> postOverview(Reading reading);

    Mono<Void> postDetails(List<ClimateMeasurement> measurements);
}
