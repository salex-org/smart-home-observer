package org.salex.hmip.observer.task;

import org.salex.hmip.observer.service.MailPublishService;
import org.salex.hmip.observer.service.OperatingAlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;

@ConditionalOnProperty("org.salex.cron.operatingAlert")
@Service
public class OperatingAlertTask {
    private static final Logger LOG = LoggerFactory.getLogger(OperatingAlertTask.class);

    private final MailPublishService mailPublishService;

    private OperatingAlertService operatingAlertService;

    public OperatingAlertTask(@Value("${org.salex.cron.operatingAlert}") String cron, MailPublishService mailPublishService, OperatingAlertService operatingAlertService) {
        this.mailPublishService = mailPublishService;
        this.operatingAlertService = operatingAlertService;
        LOG.info(String.format("Operating alert task started scheduled with cron %s", cron));
    }

    @Scheduled(cron = "${org.salex.cron.operatingAlert}")
    public void checkAndSendAlert() {
        Mono.just(operatingAlertService.retrieveEvents())
                .filter(Predicate.not(List::isEmpty))
                .flatMap(this.mailPublishService::sendOperatingAlert)
                .subscribe();
    }
}
