package org.salex.hmip.observer.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface WeatherMeasurementRepository extends JpaRepository<WeatherMeasurement, Long> {
    List<WeatherMeasurement> findByMeasuringTimeBetween(Date startTime, Date endTime);
    List<WeatherMeasurement> findByMeasuringTimeBetweenAndSensor(Date startTime, Date endTime, Sensor sensor);
}
