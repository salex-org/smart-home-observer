package org.salex.hmip.observer.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface ClimateMeasurementRepository extends JpaRepository<ClimateMeasurement, Long> {
    List<ClimateMeasurement> findByMeasuringTimeBetween(Date startTime, Date endTime);
    List<ClimateMeasurement> findByMeasuringTimeBetweenAndSensor(Date startTime, Date endTime, Sensor sensor);
//
//    @Query(
//            value = "select * from climate_measuerements",
//            nativeQuery = true
//    )
//    List<ClimateMeasurementBoundaries> getBoundariesByMeasuringTimeBetweenAndSensor(Date startTime, Date endTime, Sensor sensor);
}
