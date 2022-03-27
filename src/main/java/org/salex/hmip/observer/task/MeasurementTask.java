package org.salex.hmip.observer.task;

import org.salex.hmip.observer.data.ObserverDatabase;
import org.salex.hmip.observer.data.Reading;
import org.salex.hmip.observer.service.ClimateMeasurementService;
import org.salex.hmip.observer.service.OperatingMeasurementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@ConditionalOnProperty("org.salex.cron.measure")
@Service
public class MeasurementTask {
    private static final Logger LOG = LoggerFactory.getLogger(MeasurementTask.class);

    private final ObserverDatabase database;

    private final OperatingMeasurementService operatingMeasurementService;

    private final ClimateMeasurementService climateMeasurementService;

    public MeasurementTask(@Value("${org.salex.cron.measure}") String cron, ObserverDatabase database, OperatingMeasurementService operatingMeasurementService, ClimateMeasurementService climateMeasurementService) {
        this.database = database;
        this.operatingMeasurementService = operatingMeasurementService;
        this.climateMeasurementService = climateMeasurementService;
        LOG.info(String.format("Measurement task started scheduled with cron %s", cron));
    }

    @Scheduled(cron = "${org.salex.cron.measure}")
    public void measure() {
        Mono.just(new Reading())
                .flatMap(this.climateMeasurementService::measureClimateValues)
                .flatMap(this.operatingMeasurementService::measureOperatingValues)
                .doOnError(error -> LOG.warn(String.format("Error '%s' occurred on reading measured values, measuring will be skipped!", getRootCauseMessage(error))))
                .doOnSuccess(reading -> {
                    // this.database.addReading(reading);
                    LOG.info(String.format("New reading added to database at %s", reading.getReadingTime()));
                    for(var measurement : reading.getMeasurements()) {
                        LOG.info(String.format("\t%s", measurement));
                    }
                })
                .subscribe();
    }

    private String getRootCauseMessage(Throwable error) {
        if(error.getCause() != null) {
            return getRootCauseMessage(error.getCause());
        } else {
            return error.getMessage();
        }
    }
}
