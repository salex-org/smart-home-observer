package org.salex.hmip.observer.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "climate_measuerements")
public class ClimateMeasurement extends HomematicMeasurement {
    @Column(name = "temperature")
    private Double temperature;

    @Column(name = "humidity")
    private Double humidity;

    @Column(name = "vapor_amount")
    private Double vaporAmount;

    protected ClimateMeasurement() {}

    public ClimateMeasurement(Reading reading, Sensor sensor, Date measuringTime, Double temperature, Double humidity, Double vaporAmount) {
        super(reading, sensor, measuringTime);
        this.temperature = temperature;
        this.humidity = humidity;
        this.vaporAmount = vaporAmount;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Double getVaporAmount() {
        return vaporAmount;
    }

    public void setVaporAmount(Double vaporAmount) {
        this.vaporAmount = vaporAmount;
    }
}
