package org.salex.hmip.observer.task;

import org.salex.hmip.observer.data.ClimateMeasurement;
import org.salex.hmip.observer.data.ObserverDatabase;
import org.salex.hmip.observer.data.Sensor;
import org.salex.hmip.observer.service.MailPublishService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@ConditionalOnProperty("org.salex.cron.climateAlert")
@Service
public class ClimateAlertTask {
    private static final Logger LOG = LoggerFactory.getLogger(StatisticsTask.class);

    private final ObserverDatabase database;

    private final MailPublishService mailPublishService;

    public ClimateAlertTask(@Value("${org.salex.cron.climateAlert}") String cron, ObserverDatabase database, MailPublishService mailPublishService) {
        this.database = database;
        this.mailPublishService = mailPublishService;
        LOG.info(String.format("Climate alert task started scheduled with cron %s", cron));
    }

    @Scheduled(cron = "${org.salex.cron.climateAlert}")
    public void checkAndSendAlert() {
        final var end = new Date();
        final var start = new Date(end.getTime() - TimeUnit.HOURS.toMillis(24));
        Mono.just(this.database.getClimateMeasurements(start, end))
                .filterWhen(this::shouldSendAlarm)
                .flatMap(data -> this.mailPublishService.sendClimateAlert(start, end, data))
                .subscribe();
    }

    private Mono<Boolean> shouldSendAlarm(Map<Sensor, List<ClimateMeasurement>> data) {
        return filterRelevantMeasurements(data)
                .map(Map::values)
                .flatMapMany(Flux::fromIterable)
                .filter(Predicate.not(List::isEmpty))
                .hasElements();
    }

    private Mono<Map<Sensor, List<ClimateMeasurement>>> filterRelevantMeasurements(Map<Sensor, List<ClimateMeasurement>> data) {
        return Flux.fromIterable(data.keySet())
                .flatMap(sensor -> Mono.zip(Mono.just(sensor), filterRelevantMeasurements(data.get(sensor))))
                .collectMap(Tuple2::getT1, Tuple2::getT2);
    }

    private Mono<List<ClimateMeasurement>> filterRelevantMeasurements(List<ClimateMeasurement> data) {
        return Flux.fromIterable(data)
                .filter(climateMeasurement -> climateMeasurement.getTemperature() < 3.0 || climateMeasurement.getHumidity() < 10.0 || climateMeasurement.getHumidity() > 90.0)
                .collectList();
    }
}
