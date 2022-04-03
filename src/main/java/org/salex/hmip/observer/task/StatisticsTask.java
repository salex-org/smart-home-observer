package org.salex.hmip.observer.task;

import org.salex.hmip.observer.data.ObserverDatabase;
import org.salex.hmip.observer.service.BlogPublishService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.TimeUnit;

@ConditionalOnProperty("org.salex.cron.statistics")
@Service
public class StatisticsTask {
    private static final Logger LOG = LoggerFactory.getLogger(StatisticsTask.class);

    private final ObserverDatabase database;

    private final BlogPublishService blogPublishService;

    public StatisticsTask(@Value("${org.salex.cron.statistics}") String cron, ObserverDatabase database, BlogPublishService blogPublishService) {
        this.database = database;
        this.blogPublishService = blogPublishService;
        LOG.info(String.format("Statistics task started scheduled with cron %s", cron));
    }

    @Scheduled(cron = "${org.salex.cron.statistics}")
    public void updateStatistics() {
        final var end = new Date();
        final var start = new Date(end.getTime() - TimeUnit.DAYS.toMillis(365));
        Mono.just(this.database.getClimateMeasurementBoundaries(start, end))
                .flatMap(data -> this.blogPublishService.postHistory(start, end, data))
                .doOnError(error -> LOG.warn(String.format("Error '%s' occurred in statistics task", getRootCauseMessage(error))))
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
