package org.salex.hmip.observer.service;

import org.salex.hmip.client.HmIPClient;
import org.salex.hmip.client.HmIPState;
import org.salex.hmip.observer.data.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

public class HomematicClimateMeasurementService implements ClimateMeasurementService {
    private final HmIPClient client;

    private final Map<String, Sensor> sensors = new HashMap<>();

    public HomematicClimateMeasurementService(HmIPClient client, ObserverDatabase database) {
        this.client = client;
        for(var sensor : database.getSensors()) {
            sensors.put(sensor.getSgtin().replace("-", ""), sensor);
        }
    }

    @Override
    public Mono<Reading> measureClimateValues(Reading reading) {
        return client.loadCurrentState()
                .map(HmIPState::getDevices)
                .map(Map::values)
                .flatMapMany(Flux::fromIterable)
                .filter(device -> this.sensors.containsKey(device.getSGTIN()))
                .map(device -> reading.addMeasurement(device.getChannels().values().stream()
                            .filter(HmIPState.ClimateSensorChannel.class::isInstance)
                            .map(HmIPState.ClimateSensorChannel.class::cast)
                            .map(channel -> new ClimateMeasurement(reading, sensors.get(device.getSGTIN()), device.getStatusTimestamp(), channel.getTemperature(), (double) channel.getHumidity(), channel.getVaporAmount()))
                            .map(Measurement.class::cast)
                            .findFirst()))
                .then(Mono.just(reading));
    }
}
