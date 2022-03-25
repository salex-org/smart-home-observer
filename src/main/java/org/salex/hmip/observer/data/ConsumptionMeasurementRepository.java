package org.salex.hmip.observer.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface ConsumptionMeasurementRepository extends JpaRepository<ConsumptionMeasurement, Long> {
    List<ConsumptionMeasurement> findByMeasuringTimeBetween(Date startTime, Date endTime);
    List<ConsumptionMeasurement> findByMeasuringTimeBetweenAndSensor(Date startTime, Date endTime, Sensor sensor);
}
