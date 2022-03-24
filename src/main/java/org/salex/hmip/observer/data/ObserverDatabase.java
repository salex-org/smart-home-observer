package org.salex.hmip.observer.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class ObserverDatabase {
    private static final Logger LOG = LoggerFactory.getLogger(ObserverDatabase.class);

    private final SensorRepository sensorRepository;

    private final ReadingRepository readingRepository;

    private final ClimateMeasurementRepository climateMeasurementRepository;

    public ObserverDatabase(SensorRepository sensorRepository, ReadingRepository readingRepository, ClimateMeasurementRepository climateMeasurementRepository) throws SQLException {
        this.sensorRepository = sensorRepository;
        this.readingRepository = readingRepository;
        this.climateMeasurementRepository = climateMeasurementRepository;

        if(this.getSensors().isEmpty()) {
            LOG.info("Initializing sensor data");
            this.sensorRepository.saveAll(List.of(
                    new Sensor(1l, "Maschinenraum", Sensor.Type.HmIP_STHO, "3014-F711-A000-0EDD-89B3-A015"),
                    new Sensor(2l, "Bankraum", Sensor.Type.HmIP_STHO, "3014-F711-A000-0EDD-89B3-A112"),
                    new Sensor(3l, "Carport", Sensor.Type.HmIP_STHO, "3014-F711-A000-10DD-899E-53A0")
            ));
        }

        LOG.info("Database ready to rumble!");
    }

    /**
     * Get a list of all sensors.
     */
    @Transactional
    public List<Sensor> getSensors() {
        return this.sensorRepository.findAll();
    }

    /**
     * Add a new reading to the database.
     */
    @Transactional
    public Reading addReading(Reading reading) {
        return this.readingRepository.save(reading);
    }

    /**
     * Retrieve all climate measurement data for the last specified hours.
     */
    @Transactional
    public List<ClimateMeasurement> getClimateMeasurements(int hours) {
        return getClimateMeasurements(hours, new Date());
    }

    /**
     * Retrieve all climate measurement data for the specified hours before the given timestamp.
     */
    @Transactional
    public List<ClimateMeasurement> getClimateMeasurements(int hours, Date endTime) {
        var startTime = new Date(endTime.getTime() - TimeUnit.HOURS.toMillis(hours));
        return getClimateMeasurements(startTime, endTime);
    }

    /**
     * Retrieve all climate measurement data between the given timestamps.
     */
    @Transactional
    public List<ClimateMeasurement> getClimateMeasurements(Date startTime, Date endTime) {
        return this.climateMeasurementRepository.findByMeasuringTimeBetween(startTime, endTime);
    }

    /**
     * Retrieve all climate measurement data for the last specified hours regarding the given sensor.
     */
    @Transactional
    public List<ClimateMeasurement> getClimateMeasurements(int hours, Sensor sensor) {
        return getClimateMeasurements(hours, new Date(), sensor);
    }

    /**
     * Retrieve all climate measurement data for the specified hours before the given timestamp regarding
     * the given sensor.
     */
    @Transactional
    public List<ClimateMeasurement> getClimateMeasurements(int hours, Date endTime, Sensor sensor) {
        var startTime = new Date(endTime.getTime() - TimeUnit.HOURS.toMillis(hours));
        return getClimateMeasurements(startTime, endTime, sensor);
    }

    /**
     * Retrieve all climate measurement data between the given timestamps regarding the given sensor.
     */
    @Transactional
    public List<ClimateMeasurement> getClimateMeasurements(Date startTime, Date endTime, Sensor sensor) {
        return this.climateMeasurementRepository.findByMeasuringTimeBetweenAndSensor(startTime, endTime, sensor);
    }

    @Transactional
    public Map<Sensor, List<ClimateMeasurementBoundaries>> getClimateMeasurementBoundaries(int days) {
        // TODO implement
        return Map.of();
    }

    @Transactional
    public List<ClimateMeasurementBoundaries> getClimateMeasurementBoundaries(int days, Sensor sensor) {
        // TODO implement
        return List.of();
    }
}
