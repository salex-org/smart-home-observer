package org.salex.hmip.observer.task;

import org.salex.hmip.client.HmIPState;
import org.salex.hmip.observer.data.ClimateMeasurement;
import org.salex.hmip.observer.data.ObserverDatabase;
import org.salex.hmip.observer.data.OperatingMeasurement;
import org.salex.hmip.observer.data.Reading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.salex.hmip.client.HmIPClient;
import reactor.core.publisher.Mono;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@ConditionalOnProperty("org.salex.cron.enabled")
@Service
public class MeasurementTask {
    private static final Logger LOG = LoggerFactory.getLogger(MeasurementTask.class);

    private final ObserverDatabase database;

    private final HmIPClient homematicCient;

    private final String cpuMeasureScript;

    public MeasurementTask(ObserverDatabase database, HmIPClient homematicCient, @Value("${org.salex.cron.measure}") String cron, @Value("${org.salex.raspberry.script.cpu}") String cpuMeasureScript) {
        this.database = database;
        this.homematicCient = homematicCient;
        this.cpuMeasureScript = cpuMeasureScript;
        LOG.info(String.format("Measurement task startet scheduled with cron %s", cron));
    }

    @Scheduled(cron = "${org.salex.cron.measure}")
    public void measure() {
        this.homematicCient.loadCurrentState()
                .map(HmIPState::getDevices)
                .map(devices -> {
                    // Create reading and add climate measurements
                    final var reading = new Reading();
                    for(var sensor : this.database.getSensors()) {
                        final var device = devices.get(sensor.getSgtin().replace("-",""));
                        for(var channel : device.getChannels().values()) {
                            if(channel instanceof HmIPState.ClimateSensorChannel) {
                                final var climateSensorChannel = (HmIPState.ClimateSensorChannel) channel;
                                final var measurement = new ClimateMeasurement(reading, sensor, device.getStatusTimestamp(), climateSensorChannel.getTemperature(), Double.valueOf(climateSensorChannel.getHumidity()), climateSensorChannel.getVaporAmount());
                                reading.addMeasurement(measurement);
                            }
                        }
                    }
                    return reading;
                })
                .flatMap(reading -> {
                    if(cpuMeasureScript.isEmpty()) {
                        LOG.warn("No cpu measure script, skipping operating measurement");
                    } else {
                        try {
                            final var measurement = new OperatingMeasurement(reading, readCPUTemperature(), readCoreVoltage(), readDiskUsage(), readMemoryUsage());
                            reading.addMeasurement(measurement);
                        } catch(IOException e) {
                            return Mono.error(e);
                        }
                    }
                    return Mono.just(reading);
                })
                .subscribe(reading -> {
                    // TODO Add new reading to the database
//                    this.database.addReading(reading);
                    LOG.info(String.format("New reading added to database at %s", reading.getReadingTime()));
                    for(var measurement : reading.getMeasurements()) {
                        LOG.info(String.format("\t%s", measurement));
                    }
                });
    }

    private Double readCPUTemperature() throws IOException {
        final Process p = Runtime.getRuntime().exec(new String[] { this.cpuMeasureScript, "measure_temp" });
        final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
        final String[] result = reader.readLine().split("[=']");
        return Double.parseDouble(result[1]);
    }

    private Double readCoreVoltage() throws IOException {
        final Process p = Runtime.getRuntime().exec(new String[] { this.cpuMeasureScript, "measure_volts" });
        final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
        final String[] result = reader.readLine().split("[=V]");
        return Double.parseDouble(result[1]);
    }

    private Double readDiskUsage() throws IOException {
        final Process p = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", "df -P | grep /dev/root" });
        final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
        final String[] result = reader.readLine().split("[%,; \\t\\n\\r]+");
        return Double.parseDouble(result[4]);
    }

    private Double readMemoryUsage() throws IOException {
        final Process p = Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", "free | grep Speicher" });
        final BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream(), "UTF-8"));
        final String[] result = reader.readLine().split("[,; \\t\\n\\r]+");
        return Double.parseDouble(result[2]) / Double.parseDouble(result[1]) + 100;
    }
}
