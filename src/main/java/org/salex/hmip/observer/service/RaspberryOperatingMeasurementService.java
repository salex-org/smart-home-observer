package org.salex.hmip.observer.service;

import org.salex.hmip.observer.data.OperatingMeasurement;
import org.salex.hmip.observer.data.Reading;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class RaspberryOperatingMeasurementService implements OperatingMeasurementService {
    private final String cpuMeasureScript;

    public RaspberryOperatingMeasurementService(String cpuMeasureScript) {
        this.cpuMeasureScript = cpuMeasureScript;
    }

    @Override
    public Mono<Reading> measureOperatingValues(Reading reading) {
        try {
            var measurement = new OperatingMeasurement(reading, readCPUTemperature(), readCoreVoltage(), readDiskUsage(), readMemoryUsage());
            reading.addMeasurement(measurement);
            return Mono.just(reading);
        } catch(IOException e) {
            return Mono.error(e);
        }
    }

    private Double readCPUTemperature() throws IOException {
        final Process p = Runtime.getRuntime().exec(new String[] { this.cpuMeasureScript, "measure_temp" });
        final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
        final String[] result = reader.readLine().split("[=']");
        return Double.parseDouble(result[1]);
    }

    private Double readCoreVoltage() throws IOException {
        final Process p = Runtime.getRuntime().exec(new String[] { this.cpuMeasureScript, "measure_volts" });
        final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
        final String[] result = reader.readLine().split("[=V]");
        return Double.parseDouble(result[1]);
    }

    private Double readDiskUsage() throws IOException {
        final Process p = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", "df -P | grep /dev/root" });
        final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
        final String[] result = reader.readLine().split("[,; \\t\\n\\r]+");
        return Double.parseDouble(result[2]) / Double.parseDouble(result[1]) * 100;
    }

    private Double readMemoryUsage() throws IOException {
        final Process p = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", "free | grep Speicher" });
        final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
        final String[] result = reader.readLine().split("[,; \\t\\n\\r]+");
        return Double.parseDouble(result[2]) / Double.parseDouble(result[1]) * 100;
    }
}
