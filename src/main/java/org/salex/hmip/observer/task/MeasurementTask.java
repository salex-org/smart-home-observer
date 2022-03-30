package org.salex.hmip.observer.task;

import org.salex.hmip.observer.blog.Image;
import org.salex.hmip.observer.data.ObserverDatabase;
import org.salex.hmip.observer.data.Reading;
import org.salex.hmip.observer.service.BlogPublishService;
import org.salex.hmip.observer.service.ClimateMeasurementService;
import org.salex.hmip.observer.service.OperatingMeasurementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@ConditionalOnProperty("org.salex.cron.measure")
@Service
public class MeasurementTask {
    private static final Logger LOG = LoggerFactory.getLogger(MeasurementTask.class);

    private final ObserverDatabase database;

    private final OperatingMeasurementService operatingMeasurementService;

    private final ClimateMeasurementService climateMeasurementService;

    private final BlogPublishService blogPublishService;

    public MeasurementTask(@Value("${org.salex.cron.measure}") String cron, ObserverDatabase database, OperatingMeasurementService operatingMeasurementService, ClimateMeasurementService climateMeasurementService, BlogPublishService blogPublishService) {
        this.database = database;
        this.operatingMeasurementService = operatingMeasurementService;
        this.climateMeasurementService = climateMeasurementService;
        this.blogPublishService = blogPublishService;
        LOG.info(String.format("Measurement task started scheduled with cron %s", cron));
    }

    @Scheduled(cron = "${org.salex.cron.measure}")
    public void measure() {
        Mono.just(new Reading())
                .flatMap(this.climateMeasurementService::measureClimateValues)
                .flatMap(this.operatingMeasurementService::measureOperatingValues)
                .map(this.database::addReading)
                .flatMap(this.blogPublishService::postOverview)
                .flatMap(reading -> {
                    final var end = reading.getReadingTime();
                    final var start = new Date(end.getTime() - TimeUnit.HOURS.toMillis(24));
                    final var data = this.database.getClimateMeasurements(start, end);
                    return this.blogPublishService.postDetails(start, end, data);
                })
                .doOnError(error -> LOG.warn(String.format("Error '%s' occurred in measurement task", getRootCauseMessage(error))))
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
