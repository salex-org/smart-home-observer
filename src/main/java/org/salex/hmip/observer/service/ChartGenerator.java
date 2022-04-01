package org.salex.hmip.observer.service;

import org.salex.hmip.observer.data.ClimateMeasurement;
import org.salex.hmip.observer.data.ClimateMeasurementBoundaries;
import org.salex.hmip.observer.data.Sensor;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ChartGenerator {
    Mono<byte[]> create24HourChart(Date start, Date end, Map<Sensor, List<ClimateMeasurement>> data);

    Mono<byte[]> create365DayTemperatureChart(Date start, Date end, List<ClimateMeasurementBoundaries> data, Sensor sensor);

    Mono<byte[]> create365DayHumidityChart(Date start, Date end, List<ClimateMeasurementBoundaries> data, Sensor sensor);
}
