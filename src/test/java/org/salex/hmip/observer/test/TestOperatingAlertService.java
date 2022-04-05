package org.salex.hmip.observer.test;

import org.junit.jupiter.api.Test;
import org.salex.hmip.observer.data.OperatingMeasurement;
import org.salex.hmip.observer.data.Reading;
import org.salex.hmip.observer.service.DefaultOperatingAlertService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class TestOperatingAlertService {
    @Test
    void should_retrieve_events_only_once() {
        final var service = new DefaultOperatingAlertService();
        service.signal(new RuntimeException("Some test exception"));
        service.check(List.of(new OperatingMeasurement(new Reading(), 48.1, 2.875, 90.0, 90.0)));
        var events = service.retrieveEvents();
        assertThat(events.size()).isEqualTo(2);
        events = service.retrieveEvents();
        assertThat(events.size()).isEqualTo(0);
    }

    @Test
    void should_retrieve_error_when_signaled() {
        final var service = new DefaultOperatingAlertService();
        service.signal(new RuntimeException("Some test exception"));
        final var events = service.retrieveEvents();
        assertThat(events.size()).isEqualTo(1);
    }

    @Test
    void should_retrieve_exceedance_when_cpu_temperature_has_gone_out_of_rail() {
        final var service = new DefaultOperatingAlertService();
        service.check(List.of(new OperatingMeasurement(new Reading(), 48.1, 2.875, 90.0, 90.0)));
        final var events = service.retrieveEvents();
        assertThat(events.size()).isEqualTo(1);
    }

    @Test
    void should_retrieve_exceedance_when_memory_usage_has_gone_out_of_rail() {
        final var service = new DefaultOperatingAlertService();
        service.check(List.of(new OperatingMeasurement(new Reading(), 48.0, 2.875, 90.0, 90.1)));
        final var events = service.retrieveEvents();
        assertThat(events.size()).isEqualTo(1);
    }

    @Test
    void should_retrieve_exceedance_when_disk_usage_has_gone_out_of_rail() {
        final var service = new DefaultOperatingAlertService();
        service.check(List.of(new OperatingMeasurement(new Reading(), 48.0, 2.875, 90.1, 90.0)));
        final var events = service.retrieveEvents();
        assertThat(events.size()).isEqualTo(1);
    }

    @Test
    void should_retrieve_nothing_when_everything_is_fine() {
        final var service = new DefaultOperatingAlertService();
        service.check(List.of(new OperatingMeasurement(new Reading(), 48.0, 2.875, 90.0, 90.0)));
        final var events = service.retrieveEvents();
        assertThat(events.size()).isEqualTo(0);
    }

}
