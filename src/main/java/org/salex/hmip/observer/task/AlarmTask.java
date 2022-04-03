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
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ConditionalOnProperty("org.salex.cron.alert")
@Service
public class AlarmTask {
    private static final Logger LOG = LoggerFactory.getLogger(StatisticsTask.class);

    private final ObserverDatabase database;

    private final MailPublishService mailPublishService;

    public AlarmTask(@Value("${org.salex.cron.alert}") String cron, ObserverDatabase database, MailPublishService mailPublishService) {
        this.database = database;
        this.mailPublishService = mailPublishService;
        LOG.info(String.format("Alarm task started scheduled with cron %s", cron));
    }

    @Scheduled(cron = "${org.salex.cron.alert}")
    public void checkAndSendAlarm() {
        final var end = new Date();
        final var start = new Date(end.getTime() - TimeUnit.HOURS.toMillis(24));
        Mono.just(this.database.getClimateMeasurements(start, end))
                .filter(this::shouldSendAlarm)
                .flatMap(data -> this.mailPublishService.sendClimateAlarm(start, end, data))
                .doOnError(error -> LOG.warn(String.format("Error '%s' occurred in alarm task", getRootCauseMessage(error))))
                .subscribe();
    }

    private String getRootCauseMessage(Throwable error) {
        if(error.getCause() != null) {
            return getRootCauseMessage(error.getCause());
        } else {
            return error.getMessage();
        }
    }

    private boolean shouldSendAlarm(Map<Sensor, List<ClimateMeasurement>> data) {
        return data.values().stream()
                .map(sensorData -> sensorData.stream().min(Comparator.comparing(ClimateMeasurement::getTemperature)).orElseThrow())
                .map(ClimateMeasurement::getTemperature)
                .filter(minTemperature -> minTemperature < 3.0)
                .count() > 0;
    }
}
