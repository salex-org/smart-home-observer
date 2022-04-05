package org.salex.hmip.observer.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salex.hmip.observer.data.*;
import org.salex.hmip.observer.service.MailPublishService;
import org.salex.hmip.observer.service.OperatingAlertService;
import org.salex.hmip.observer.task.OperatingAlertTask;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verifyNoInteractions;

public class TestOperatingAlertTask {
    private OperatingAlertService operatingAlertService;

    private MailPublishService mailPublishService;

    @BeforeEach
    void setup() {
        operatingAlertService = mock(OperatingAlertService.class);
        mailPublishService = mock(MailPublishService.class);
    }

    @Test
    void should_send_alarm_when_error_has_occurred() {
        final List<OperatingAlertService.Event> events = List.of( new OperatingAlertService.Error(new RuntimeException("Some test exception")));
        when(operatingAlertService.retrieveEvents()).thenReturn(events);
        when(mailPublishService.sendOperatingAlert(any())).thenReturn(Mono.just(events));
        final var task = new OperatingAlertTask("test-cron", mailPublishService, operatingAlertService);
        task.checkAndSendAlert();
        verify(operatingAlertService, times(1)).retrieveEvents();
        verify(mailPublishService, times(1)).sendOperatingAlert(any());
        verifyNoMoreInteractions(operatingAlertService);
        verifyNoMoreInteractions(mailPublishService);
    }

    @Test
    void should_send_alarm_when_exceedance_has_occurred() {
        final var reading = new Reading();
        final var measurement = new OperatingMeasurement(reading, 48.1, 2.875, 90.0, 90.0);
        final List<OperatingAlertService.Event> events = List.of( new OperatingAlertService.Exceedance(measurement));
        when(operatingAlertService.retrieveEvents()).thenReturn(events);
        when(mailPublishService.sendOperatingAlert(any())).thenReturn(Mono.just(events));
        final var task = new OperatingAlertTask("test-cron", mailPublishService, operatingAlertService);
        task.checkAndSendAlert();
        verify(operatingAlertService, times(1)).retrieveEvents();
        verify(mailPublishService, times(1)).sendOperatingAlert(any());
        verifyNoMoreInteractions(operatingAlertService);
        verifyNoMoreInteractions(mailPublishService);
    }

    @Test
    void should_do_nothing_when_everything_is_fine() {
        final List<OperatingAlertService.Event> events = new ArrayList<>();
        when(operatingAlertService.retrieveEvents()).thenReturn(events);
        final var task = new OperatingAlertTask("test-cron", mailPublishService, operatingAlertService);
        task.checkAndSendAlert();
        verify(operatingAlertService, times(1)).retrieveEvents();
        verifyNoMoreInteractions(operatingAlertService);
        verifyNoInteractions(mailPublishService);
    }
}
