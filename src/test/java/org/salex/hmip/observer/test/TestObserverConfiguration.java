package org.salex.hmip.observer.test;

import org.salex.hmip.observer.service.DefaultOperatingAlertService;
import org.salex.hmip.observer.service.OperatingAlertService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestObserverConfiguration {
    @Bean
    OperatingAlertService createTestOperatingAlertService() {
        return new DefaultOperatingAlertService();
    }
}
