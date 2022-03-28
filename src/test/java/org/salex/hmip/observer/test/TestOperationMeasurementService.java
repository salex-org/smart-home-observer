package org.salex.hmip.observer.test;

import org.junit.jupiter.api.Test;
import org.salex.hmip.observer.data.OperatingMeasurement;
import org.salex.hmip.observer.data.Reading;
import org.salex.hmip.observer.service.RaspberryOperatingMeasurementService;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestOperationMeasurementService {
    @Test
    void should_return_operating_measurement_data() throws IOException {
        final var operatingSystemAccess = mock(RaspberryOperatingMeasurementService.OperatingSystemAccess.class);
        when(operatingSystemAccess.runCommand(new String[] { "test-script", "measure_temp" })).thenReturn("temp=44.5'C");
        when(operatingSystemAccess.runCommand(new String[] { "test-script", "measure_volts" })).thenReturn("volt=1.2875V");
        when(operatingSystemAccess.runCommand(new String[] { "/bin/sh", "-c", "df -P | grep /dev/root" })).thenReturn("/dev/root       30388284 3502848  25617036   13% /");
        when(operatingSystemAccess.runCommand(new String[] { "/bin/sh", "-c", "free | grep Speicher" })).thenReturn("Speicher:     931736      400332       80136         308      451268      469484");
        final var service = new RaspberryOperatingMeasurementService("test-script", operatingSystemAccess);
        final var reading = new Reading();
        StepVerifier
                .create(service.measureOperatingValues(reading))
                .expectNext(reading)
                .verifyComplete();
        assertThat(reading.getMeasurements().size()).isEqualTo(1);
        assertThat(((OperatingMeasurement)reading.getMeasurements().get(0)).getCpuTemperature()).isEqualTo(44.5);
        assertThat(((OperatingMeasurement)reading.getMeasurements().get(0)).getCoreVoltage()).isEqualTo(1.2875);
        assertThat(((OperatingMeasurement)reading.getMeasurements().get(0)).getDiskUsage()).isEqualTo(11.526968748877033);
        assertThat(((OperatingMeasurement)reading.getMeasurements().get(0)).getMemoryUsage()).isEqualTo(42.96624795006311);
    }
}
