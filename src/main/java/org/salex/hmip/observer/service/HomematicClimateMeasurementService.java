package org.salex.hmip.observer.service;

import org.salex.hmip.client.HmIPClient;
import org.salex.hmip.client.HmIPState;
import org.salex.hmip.observer.data.ClimateMeasurement;
import org.salex.hmip.observer.data.Reading;
import org.salex.hmip.observer.data.Sensor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomematicClimateMeasurementService implements ClimateMeasurementService {
    private final HmIPClient client;

    public HomematicClimateMeasurementService(HmIPClient client) {
        this.client = client;
    }

    @Override
    public Mono<Reading> measureClimateValues(Reading reading, List<Sensor> sensors) {
        final var sensorMap = new HashMap<String, Sensor>();
        for(var sensor : sensors) {
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
