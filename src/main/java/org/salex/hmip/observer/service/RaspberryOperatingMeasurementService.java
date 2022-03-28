package org.salex.hmip.observer.service;

import org.salex.hmip.observer.data.OperatingMeasurement;
import org.salex.hmip.observer.data.Reading;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class RaspberryOperatingMeasurementService implements OperatingMeasurementService {
    public static class OperatingSystemAccess {

        public String runCommand(String[] command) throws IOException {
            final Process p = Runtime.getRuntime().exec(command);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
            return reader.readLine();
        }
    }

    private final String cpuMeasureScript;

    private final OperatingSystemAccess operatingSystemAccess;

    public RaspberryOperatingMeasurementService(String cpuMeasureScript) {
        this(cpuMeasureScript, new OperatingSystemAccess());
    }

    public RaspberryOperatingMeasurementService(String cpuMeasureScript, OperatingSystemAccess operatingSystemAccess) {
        this.cpuMeasureScript = cpuMeasureScript;
        this.operatingSystemAccess = operatingSystemAccess;
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
        final var result = this.operatingSystemAccess.runCommand(new String[] { this.cpuMeasureScript, "measure_temp" }).split("[=']");
        return Double.parseDouble(result[1]);
    }

    private Double readCoreVoltage() throws IOException {
        final var result = this.operatingSystemAccess.runCommand(new String[] { this.cpuMeasureScript, "measure_volts" }).split("[=V]");
        return Double.parseDouble(result[1]);
    }

    private Double readDiskUsage() throws IOException {
        final var result = this.operatingSystemAccess.runCommand(new String[] { "/bin/sh", "-c", "df -P | grep /dev/root" }).split("[,; \\t\\n\\r]+");
        return Double.parseDouble(result[2]) / Double.parseDouble(result[1]) * 100;
    }

    private Double readMemoryUsage() throws IOException {
        final var result = this.operatingSystemAccess.runCommand(new String[] { "/bin/sh", "-c", "free | grep Speicher" }).split("[,; \\t\\n\\r]+");
        return Double.parseDouble(result[2]) / Double.parseDouble(result[1]) * 100;
    }
}
