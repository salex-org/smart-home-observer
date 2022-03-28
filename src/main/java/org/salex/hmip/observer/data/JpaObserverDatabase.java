package org.salex.hmip.observer.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class JpaObserverDatabase implements ObserverDatabase {
    private static final Logger LOG = LoggerFactory.getLogger(JpaObserverDatabase.class);

    private final SensorRepository sensorRepository;

    private final ReadingRepository readingRepository;

    private final ClimateMeasurementRepository climateMeasurementRepository;

    public JpaObserverDatabase(SensorRepository sensorRepository, ReadingRepository readingRepository, ClimateMeasurementRepository climateMeasurementRepository) {
        this.sensorRepository = sensorRepository;
        this.readingRepository = readingRepository;
        this.climateMeasurementRepository = climateMeasurementRepository;

        if(this.getSensors().isEmpty()) {
            LOG.info("Initializing sensor data");
            this.sensorRepository.saveAll(List.of(
                    new Sensor(1L, "Maschinenraum", Sensor.Type.HmIP_STHO, "3014-F711-A000-0EDD-89B3-A015"),
                    new Sensor(2L, "Bankraum", Sensor.Type.HmIP_STHO, "3014-F711-A000-0EDD-89B3-A112"),
                    new Sensor(3L, "Carport", Sensor.Type.HmIP_STHO, "3014-F711-A000-10DD-899E-53A0")
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
    public List<ClimateMeasurement> getClimateMeasurements(int hours) {
        return getClimateMeasurements(hours, new Date());
    }

    /**
     * Retrieve all climate measurement data for the specified hours before the given timestamp.
     */
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
    public List<ClimateMeasurement> getClimateMeasurements(int hours, Sensor sensor) {
        return getClimateMeasurements(hours, new Date(), sensor);
    }

    /**
     * Retrieve all climate measurement data for the specified hours before the given timestamp regarding
     * the given sensor.
     */
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

    /**
     * Evaluates the climate data of the last specified days and returns the maximum and minimum values per day
     * for each sensor.
     */
    public Map<Sensor, List<ClimateMeasurementBoundaries>> getClimateMeasurementBoundaries(int days) {
        return getClimateMeasurementBoundaries(days, new Date());
    }

    /**
     * Evaluates the climate data of the specified days before the given timestamp and returns the maximum and
     * minimum values per day for each sensor.
     */
    public Map<Sensor, List<ClimateMeasurementBoundaries>> getClimateMeasurementBoundaries(int days, Date endTime) {
        var startTime = new Date(endTime.getTime() - TimeUnit.DAYS.toMillis(days));
        return getClimateMeasurementBoundaries(startTime, endTime);
    }

    /**
     * Evaluates the climate data of the specified period and returns the maximum and minimum values per day
     * for each sensor.
     */
    @Transactional
    public Map<Sensor, List<ClimateMeasurementBoundaries>> getClimateMeasurementBoundaries(Date startTime, Date endTime) {
        var result = new HashMap<Sensor, List<ClimateMeasurementBoundaries>>();
        var sensorLookup = new HashMap<Long, Sensor>();
        for(var sensor : this.sensorRepository.findAll()) {
            sensorLookup.put(sensor.getId(), sensor);
            result.put(sensor, new ArrayList<>());
        }
        for(var boundaries :  this.climateMeasurementRepository.findBoundariesByMeasuringTimeBetween(startTime, endTime)) {
            result.get(sensorLookup.get(boundaries.getSensorId())).add(boundaries);
        }
        return result;
    }
}
