package org.salex.hmip.observer.service;

import org.salex.hmip.observer.data.OperatingMeasurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DefaultOperatingAlertService implements OperatingAlertService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultOperatingAlertService.class);

    private final List<Event> events;

    public DefaultOperatingAlertService() {
        this.events = new ArrayList<>();
    }

    @Override
    public void signal(Throwable error) {
        LOG.warn(error.getMessage());
        LOG.debug(error.getMessage(), error);
        synchronized (this.events) {
            events.add(new Error(error));
        }
    }

    @Override
    public void check(List<OperatingMeasurement> measurements) {
        synchronized (this.events) {
            this.events.addAll(measurements.stream()
                .filter(measurement -> measurement.getCpuTemperature() > 48.0 || measurement.getMemoryUsage() > 90.0 || measurement.getDiskUsage() > 90.0)
                .map(Exceedance::new)
                .collect(Collectors.toList()));
        }
    }

    public List<Event> retrieveEvents() {
        synchronized (this.events) {
            var events = List.copyOf(this.events);
            this.events.clear();
            return events;
        }
    }


}
