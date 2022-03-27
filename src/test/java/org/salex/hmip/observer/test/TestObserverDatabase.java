package org.salex.hmip.observer.test;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.salex.hmip.observer.data.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@DataJpaTest
@Import(JpaObserverDatabase.class)
public class TestObserverDatabase {
    @Autowired
    JpaObserverDatabase database;

    @Test
    public void testGetSensors() {
        var sensors = this.database.getSensors();
        Assertions.assertEquals(3, sensors.size());
    }

    @Test
    public void testAddReading() {
        // Prepare test
        final var sensor = this.database.getSensors().iterator().next();
        final var readingTime = new Date();
        final var reading = createReading(readingTime, sensor);

        // Check if the IDs of new data are null
        Assertions.assertNull(reading.getId());
        for(var measurement : reading.getMeasurements()) {
            Assertions.assertNull(measurement.getId());
        }

        // Store data
        database.addReading(reading);

        // Check if the IDs of new data are now filled
        Assertions.assertNotNull(reading.getId());
        for(var measurement : reading.getMeasurements()) {
            Assertions.assertNotNull(measurement.getId());
        }
    }

    @Test
    public void testGetClimateMeasurement() {
        // Prepare test
        final var now = new Date();
        final var oneHourAgo = new Date(now.getTime() - TimeUnit.HOURS.toMillis(1));
        final var threeHoursAgo = new Date(now.getTime() - TimeUnit.HOURS.toMillis(3));
        final var sensor = this.database.getSensors().iterator().next();

        // Create some data in the database
        database.addReading(createReading(now, sensor));
        database.addReading(createReading(oneHourAgo, sensor));
        database.addReading(createReading(threeHoursAgo, sensor));

        // Read the measurements
        var measurements = database.getClimateMeasurements(2);

        // Check if the correct measurements are found
        var measuringTimes = measurements.stream().map(HomematicMeasurement::getMeasuringTime).toList();
        Assertions.assertEquals(2, measuringTimes.size());
        Assertions.assertTrue(measuringTimes.stream().anyMatch(t -> t.compareTo(now) == 0));
        Assertions.assertTrue(measuringTimes.stream().anyMatch(t -> t.compareTo(oneHourAgo) == 0));

        // Check if all attributes are filled
        var measurement = measurements.stream().filter(m -> m.getMeasuringTime().compareTo(now) == 0).findFirst().orElse(null);
        Assertions.assertNotNull(measurement);
        Assertions.assertEquals(now, measurement.getMeasuringTime());
        Assertions.assertEquals(sensor, measurement.getSensor());
        Assertions.assertEquals(1.0, measurement.getTemperature());
        Assertions.assertEquals(2.0, measurement.getHumidity());
        Assertions.assertEquals(3.0, measurement.getVaporAmount());
    }

    @Test
    public void testGetClimateMeasurementRegardingSensor() {
        // Prepare test
        final var now = new Date();
        final var oneHourAgo = new Date(now.getTime() - TimeUnit.HOURS.toMillis(1));
        final var threeHoursAgo = new Date(now.getTime() - TimeUnit.HOURS.toMillis(3));
        final var sensorIterator = this.database.getSensors().iterator();
        final var firstSensor = sensorIterator.next();
        final var secondSensor = sensorIterator.next();

        // Create some data in the database
        database.addReading(createReading(now, firstSensor));
        database.addReading(createReading(now, secondSensor));
        database.addReading(createReading(oneHourAgo, firstSensor));
        database.addReading(createReading(threeHoursAgo, firstSensor));
        database.addReading(createReading(threeHoursAgo, secondSensor));

        // Read the measurements for first sensor
        var measurements = database.getClimateMeasurements(2, firstSensor);

        // Check if the correct measurements are found
        var measuringTimes = measurements.stream().map(HomematicMeasurement::getMeasuringTime).collect(Collectors.toList());
        Assertions.assertEquals(2, measuringTimes.size());
        Assertions.assertTrue(measuringTimes.stream().anyMatch(t -> t.compareTo(now) == 0));
        Assertions.assertTrue(measuringTimes.stream().anyMatch(t -> t.compareTo(oneHourAgo) == 0));

        // Read the measurements for first sensor
        measurements = database.getClimateMeasurements(2, secondSensor);

        // Check if the correct measurements are found
        measuringTimes = measurements.stream().map(HomematicMeasurement::getMeasuringTime).collect(Collectors.toList());
        Assertions.assertEquals(1, measuringTimes.size());
        Assertions.assertTrue(measuringTimes.stream().anyMatch(t -> t.compareTo(now) == 0));
    }

    @Test
    public void testGetClimateMeasurementBoundaries() {
        // Prepare test
        final var now = new Date();
        final var tenMinutesAgo = new Date(now.getTime() - TimeUnit.MINUTES.toMillis(10));
        final var yesterday = new Date(now.getTime() - TimeUnit.DAYS.toMillis(1));
        final var threeDaysAgo = new Date(now.getTime() - TimeUnit.DAYS.toMillis(3));
        final var sensorIterator = this.database.getSensors().iterator();
        final var firstSensor = sensorIterator.next();
        final var secondSensor = sensorIterator.next();

        // Create some data in the database
        database.addReading(createReading(now, firstSensor));
        var maxReading = new Reading(tenMinutesAgo);
        maxReading.addMeasurement(new ClimateMeasurement(maxReading, firstSensor, tenMinutesAgo, 10.0, 20.0, 30.0));
        database.addReading(maxReading);
        database.addReading(createReading(now, secondSensor));
        database.addReading(createReading(yesterday, firstSensor));
        database.addReading(createReading(threeDaysAgo, firstSensor));
        database.addReading(createReading(threeDaysAgo, secondSensor));

        // Read the measurement boundaries
        var boundaries = database.getClimateMeasurementBoundaries(2);

        // Check if the correct boundaries are found
        Assertions.assertNotNull(boundaries);
        Assertions.assertEquals(3, boundaries.size());
        Assertions.assertEquals(2, boundaries.get(firstSensor).size());
        Assertions.assertEquals(1, boundaries.get(secondSensor).size());

        // Check if all attributes are filled
        var today = today();
        var examinee = boundaries.get(firstSensor).stream().filter(b -> b.getDay().compareTo(today) == 0).findFirst().orElse(null);
        Assertions.assertNotNull(examinee);
        Assertions.assertNotNull(examinee.getSensorId());
        Assertions.assertEquals(firstSensor.getId(), examinee.getSensorId());
        Assertions.assertNotNull(examinee.getDay());
        Assertions.assertNotNull(examinee.getMinimumTemperature());
        Assertions.assertEquals(1.0, examinee.getMinimumTemperature());
        Assertions.assertNotNull(examinee.getMaximumTemperature());
        Assertions.assertEquals(10.0, examinee.getMaximumTemperature());
        Assertions.assertNotNull(examinee.getMinimumHumidity());
        Assertions.assertEquals(2.0, examinee.getMinimumHumidity());
        Assertions.assertNotNull(examinee.getMaximumHumidity());
        Assertions.assertEquals(20.0, examinee.getMaximumHumidity());
        Assertions.assertNotNull(examinee.getMinimumVaporAmount());
        Assertions.assertEquals(3.0, examinee.getMinimumVaporAmount());
        Assertions.assertNotNull(examinee.getMaximumVaporAmount());
        Assertions.assertEquals(30.0, examinee.getMaximumVaporAmount());
    }

    private Date today() {
        final var calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Reading createReading(Date readingTime, Sensor sensor) {
        final var reading = new Reading(readingTime);
        reading.addMeasurement(createOperatingMeasurement(reading));
        reading.addMeasurement(createClimateMeasurement(reading, sensor, readingTime));
        reading.addMeasurement(createConsumptionMeasurement(reading, sensor, readingTime));
        return reading;
    }

    private OperatingMeasurement createOperatingMeasurement(Reading reading) {
        return new OperatingMeasurement(reading, 1.0, 2.0, 3.0, 4.0);
    }

    private ClimateMeasurement createClimateMeasurement(Reading reading, Sensor sensor, Date measuringTime) {
        return new ClimateMeasurement(reading, sensor, measuringTime, 1.0, 2.0, 3.0);
    }

    private ConsumptionMeasurement createConsumptionMeasurement(Reading reading, Sensor sensor, Date measuringTime) {
        return new ConsumptionMeasurement(reading, sensor, measuringTime, 1.0);
    }

}
