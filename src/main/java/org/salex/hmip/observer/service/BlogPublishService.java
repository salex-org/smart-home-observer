package org.salex.hmip.observer.service;

import org.salex.hmip.observer.data.ClimateMeasurement;
import org.salex.hmip.observer.data.ClimateMeasurementBoundaries;
import org.salex.hmip.observer.data.Reading;
import org.salex.hmip.observer.data.Sensor;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface BlogPublishService {
    Mono<Reading> postOverview(Reading reading);

    Mono<Map<Sensor, List<ClimateMeasurement>>> postDetails(Date start, Date end, Map<Sensor, List<ClimateMeasurement>> data);

    Mono<Map<Sensor, List<ClimateMeasurementBoundaries>>> postHistory(Date start, Date end, Map<Sensor, List<ClimateMeasurementBoundaries>> data);
}
