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

    private final ObserverDatabase database;

    public HomematicClimateMeasurementService(HmIPClient client, ObserverDatabase database) {
        this.client = client;
        this.database = database;
    }

    @Override
    public Mono<Reading> measureClimateValues(Reading reading) {
        final var sensorMap = new HashMap<String, Sensor>();
        for(var sensor : this.database.getSensors()) {
            sensorMap.put(sensor.getSgtin().replace("-", ""), sensor);
        }
        return client.loadCurrentState()
                .map(HmIPState::getDevices)
                .map(Map::values)
                .flatMapMany(Flux::fromIterable)
                .filter(device -> sensorMap.containsKey(device.getSGTIN()))
                .map(device -> reading.addMeasurement(device.getChannels().values().stream()
                            .filter(HmIPState.ClimateSensorChannel.class::isInstance)
                            .map(HmIPState.ClimateSensorChannel.class::cast)
                            .map(channel -> new ClimateMeasurement(reading, sensorMap.get(device.getSGTIN()), device.getStatusTimestamp(), channel.getTemperature(), (double) channel.getHumidity(), channel.getVaporAmount()))
                            .map(Measurement.class::cast)
                            .findFirst()))
                .then(Mono.just(reading));
    }
}
