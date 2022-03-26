package org.salex.hmip.observer.service;

import org.salex.hmip.client.HmIPClient;
import org.salex.hmip.client.HmIPState;
import org.salex.hmip.observer.data.ClimateMeasurement;
import org.salex.hmip.observer.data.ObserverDatabase;
import org.salex.hmip.observer.data.Reading;
import org.salex.hmip.observer.data.Sensor;
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
                .map(device -> {
                   for(var channel : device.getChannels().values()) {
                       if(channel instanceof final HmIPState.ClimateSensorChannel climateSensorChannel) {
                           final var measurement = new ClimateMeasurement(reading, sensorMap.get(device.getSGTIN()), device.getStatusTimestamp(), climateSensorChannel.getTemperature(), (double) climateSensorChannel.getHumidity(), climateSensorChannel.getVaporAmount());
                           reading.addMeasurement(measurement);
                       }
                   }
                   return device;
                })
                .then(Mono.just(reading));
    }
}
