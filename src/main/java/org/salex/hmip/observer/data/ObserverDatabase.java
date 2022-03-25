package org.salex.hmip.observer.data;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ObserverDatabase {
    /**
     * Get a list of all sensors.
     */
    List<Sensor> getSensors();

    /**
     * Add a new reading to the database.
     */
    void addReading(Reading reading);

    /**
     * Retrieve all climate measurement data for the last specified hours.
     */
    List<ClimateMeasurement> getClimateMeasurements(int hours);

    /**
     * Retrieve all climate measurement data for the specified hours before the given timestamp.
     */
    List<ClimateMeasurement> getClimateMeasurements(int hours, Date endTime);

    /**
     * Retrieve all climate measurement data between the given timestamps.
     */
    List<ClimateMeasurement> getClimateMeasurements(Date startTime, Date endTime);

    /**
     * Retrieve all climate measurement data for the last specified hours regarding the given sensor.
     */
    List<ClimateMeasurement> getClimateMeasurements(int hours, Sensor sensor);

    /**
     * Retrieve all climate measurement data for the specified hours before the given timestamp regarding
     * the given sensor.
     */
    List<ClimateMeasurement> getClimateMeasurements(int hours, Date endTime, Sensor sensor);

    /**
     * Retrieve all climate measurement data between the given timestamps regarding the given sensor.
     */
    List<ClimateMeasurement> getClimateMeasurements(Date startTime, Date endTime, Sensor sensor);

    /**
     * Evaluates the climate data of the last specified days and returns the maximum and minimum values per day
     * for each sensor.
     */
    Map<Sensor, List<ClimateMeasurementBoundaries>> getClimateMeasurementBoundaries(int days);

    /**
     * Evaluates the climate data of the specified days before the given timestamp and returns the maximum and
     * minimum values per day for each sensor.
     */
    Map<Sensor, List<ClimateMeasurementBoundaries>> getClimateMeasurementBoundaries(int days, Date endTime);

    /**
     * Evaluates the climate data of the specified period and returns the maximum and minimum values per day
     * for each sensor.
     */
    Map<Sensor, List<ClimateMeasurementBoundaries>> getClimateMeasurementBoundaries(Date startTime, Date endTime);
}
