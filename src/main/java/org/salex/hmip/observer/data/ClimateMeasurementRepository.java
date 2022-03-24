package org.salex.hmip.observer.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface ClimateMeasurementRepository extends JpaRepository<ClimateMeasurement, Long> {
    List<ClimateMeasurement> findByMeasuringTimeBetween(Date startTime, Date endTime);
    List<ClimateMeasurement> findByMeasuringTimeBetweenAndSensor(Date startTime, Date endTime, Sensor sensor);

    @Query(
            value = "select date(measuring_time) as day, sensor as sensorId, " +
                    "max(temperature) as maximumTemperature, min(temperature) as minimumTemperature, " +
                    "max(humidity) as maximumHumidity, min(humidity) as minimumHumidity, " +
                    "max(vapor_amount) as maximumVaporAmount, min(vapor_amount) as minimumVaporAmount, " +
                    "max(wind_speed) as maximumWindSpeed, min(wind_speed) as minimumWindSpeed, " +
                    "max(brightness) as maximumBrightness, min(brightness) as minimumBrightness, " +
                    "max(rainfall) as maximumRainfall, min(rainfall) as minimumRainfall " +
                    "from climate_measuerements where date(measuring_time) between :start_time and :end_time " +
                    "group by date(measuring_time), sensor",
            nativeQuery = true
    )
    List<ClimateMeasurementBoundaries> findBoundariesByMeasuringTimeBetween(@Param("start_time") Date startTime, @Param("end_time") Date endTime);
}
