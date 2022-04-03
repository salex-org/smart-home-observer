package org.salex.hmip.observer.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salex.hmip.observer.data.*;
import org.salex.hmip.observer.service.BlogPublishService;
import org.salex.hmip.observer.service.MailPublishService;
import org.salex.hmip.observer.task.AlarmTask;
import org.salex.hmip.observer.task.MeasurementTask;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

public class TestAlarmTask {
    private ObserverDatabase database;

    private MailPublishService mailPublishService;

    @BeforeEach
    void setup() {
        database = mock(ObserverDatabase.class);
        mailPublishService = mock(MailPublishService.class);
    }

    @Test
    void should_send_alarm_when_climate_has_gone_off_the_rails() {
        final var now = new Date();
        final var tenMinutesAgo = new Date(now.getTime() - TimeUnit.MINUTES.toMillis(10));
        final var twentyMinutesAgo = new Date(now.getTime() - TimeUnit.MINUTES.toMillis(20));
        final var reading = new Reading(now);
        final var firstSensor = new Sensor(1L, "Testsensor 1", Sensor.Type.HmIP_STHO, "test-sgtin-1", "#FF0000");
        final var secondSensor = new Sensor(2L, "Testsensor 2", Sensor.Type.HmIP_STHO, "test-sgtin-2", "#00FF00");
        final var data = Map.of(
                firstSensor, List.of(
                        new ClimateMeasurement(reading, firstSensor, twentyMinutesAgo, 11.2, 52.7, 5.2386758493768),
                        new ClimateMeasurement(reading, firstSensor, tenMinutesAgo, 13.2, 42.7, 5.2386758493768),
                        new ClimateMeasurement(reading, firstSensor, now, 12.2, 32.7, 5.2386758493768)
                ),
                secondSensor, List.of(
                        new ClimateMeasurement(reading, secondSensor, twentyMinutesAgo, 21.2, 82.7, 5.2386758493768),
                        new ClimateMeasurement(reading, secondSensor, tenMinutesAgo, 2.2, 72.7, 5.2386758493768),
                        new ClimateMeasurement(reading, secondSensor, now, 22.2, 62.7, 5.2386758493768)
                )
        );
        when(database.getClimateMeasurements(any(Date.class), any(Date.class))).thenReturn(data);
        when(mailPublishService.sendClimateAlarm(any(Date.class), any(Date.class), any())).thenReturn(Mono.just(data));
        final var task = new AlarmTask("test-cron", database, mailPublishService);
        task.checkAndSendAlarm();
        verify(database, times(1)).getClimateMeasurements(any(Date.class), any(Date.class));
        verify(mailPublishService, times(1)).sendClimateAlarm(any(Date.class), any(Date.class), any());
        verifyNoMoreInteractions(database);
        verifyNoMoreInteractions(mailPublishService);
    }

    @Test
    void should_do_nothing_when_climate_is_fine() {
        final var now = new Date();
        final var tenMinutesAgo = new Date(now.getTime() - TimeUnit.MINUTES.toMillis(10));
        final var twentyMinutesAgo = new Date(now.getTime() - TimeUnit.MINUTES.toMillis(20));
        final var reading = new Reading(now);
        final var firstSensor = new Sensor(1L, "Testsensor 1", Sensor.Type.HmIP_STHO, "test-sgtin-1", "#FF0000");
        final var secondSensor = new Sensor(2L, "Testsensor 2", Sensor.Type.HmIP_STHO, "test-sgtin-2", "#00FF00");
        final var data = Map.of(
                firstSensor, List.of(
                        new ClimateMeasurement(reading, firstSensor, twentyMinutesAgo, 11.2, 52.7, 5.2386758493768),
                        new ClimateMeasurement(reading, firstSensor, tenMinutesAgo, 13.2, 42.7, 5.2386758493768),
                        new ClimateMeasurement(reading, firstSensor, now, 12.2, 32.7, 5.2386758493768)
                ),
                secondSensor, List.of(
                        new ClimateMeasurement(reading, secondSensor, twentyMinutesAgo, 21.2, 82.7, 5.2386758493768),
                        new ClimateMeasurement(reading, secondSensor, tenMinutesAgo, 23.2, 72.7, 5.2386758493768),
                        new ClimateMeasurement(reading, secondSensor, now, 22.2, 62.7, 5.2386758493768)
                )
        );
        when(database.getClimateMeasurements(any(Date.class), any(Date.class))).thenReturn(data);
        when(mailPublishService.sendClimateAlarm(any(Date.class), any(Date.class), any())).thenReturn(Mono.just(data));
        final var task = new AlarmTask("test-cron", database, mailPublishService);
        task.checkAndSendAlarm();
        verify(database, times(1)).getClimateMeasurements(any(Date.class), any(Date.class));
        verifyNoMoreInteractions(database);
        verifyNoInteractions(mailPublishService);
    }
}
