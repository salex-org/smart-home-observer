package org.salex.hmip.observer.data;

import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.NamedNativeQuery;
import javax.persistence.SqlResultSetMapping;
import java.util.Date;

@NamedNativeQuery(name = "ClimateMeasurementBoundariesQuery", query = "", resultSetMapping = "ClimateMeasurementBoundariesMapping")
@SqlResultSetMapping(name = "ClimateMeasurementBoundariesMapping", classes = @ConstructorResult(
        targetClass = ClimateMeasurementBoundaries.class,
        columns = {
               @ColumnResult(name = "minimumTemperature", type = Double.class),
               @ColumnResult(name = "maximumTemperature", type = Double.class)
        }
))
public class ClimateMeasurementBoundaries {
    private Double minimumTemperature;
    private Double maximumTemperature;
    private Double minimumHumidity;
    private Double maximumHumidity;
    private Double minimumVaporAmount;
    private Double maximumVaporAmount;
    private Double minimumWindSpeed;
    private Double maximumWindSpeed;
    private Double minimumBrightness;
    private Double maximumBrightness;
    private Double minimumRainfall;
    private Double maximumRainfall;
    private Sensor sensor;
    private Date day;

    protected ClimateMeasurementBoundaries() {}

    public ClimateMeasurementBoundaries(double minimumTemperature, Double maximumTemperature, Double minimumHumidity, Double maximumHumidity, Double minimumVaporAmount, Double maximumVaporAmount, Double minimumWindSpeed, Double maximumWindSpeed, Double minimumBrightness, Double maximumBrightness, Double minimumRainfall, Double maximumRainfall, Sensor sensor, Date day) {
        this.minimumTemperature = minimumTemperature;
        this.maximumTemperature = maximumTemperature;
        this.minimumHumidity = minimumHumidity;
        this.maximumHumidity = maximumHumidity;
        this.minimumVaporAmount = minimumVaporAmount;
        this.maximumVaporAmount = maximumVaporAmount;
        this.minimumWindSpeed = minimumWindSpeed;
        this.maximumWindSpeed = maximumWindSpeed;
        this.minimumBrightness = minimumBrightness;
        this.maximumBrightness = maximumBrightness;
        this.minimumRainfall = minimumRainfall;
        this.maximumRainfall = maximumRainfall;
        this.sensor = sensor;
        this.day = day;
    }

    public Double getMinimumTemperature() {
        return minimumTemperature;
    }

    public Double getMaximumTemperature() {
        return maximumTemperature;
    }

    public Double getMinimumHumidity() {
        return minimumHumidity;
    }

    public Double getMaximumHumidity() {
        return maximumHumidity;
    }

    public Double getMinimumVaporAmount() {
        return minimumVaporAmount;
    }

    public Double getMaximumVaporAmount() {
        return maximumVaporAmount;
    }

    public Double getMinimumWindSpeed() {
        return minimumWindSpeed;
    }

    public Double getMaximumWindSpeed() {
        return maximumWindSpeed;
    }

    public Double getMinimumBrightness() {
        return minimumBrightness;
    }

    public Double getMaximumBrightness() {
        return maximumBrightness;
    }

    public Double getMinimumRainfall() {
        return minimumRainfall;
    }

    public Double getMaximumRainfall() {
        return maximumRainfall;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public Date getDay() {
        return day;
    }
}
