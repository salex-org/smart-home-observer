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

    @Column(name = "wind_speed")
    private Double windSpeed;

    @Column(name = "wind_direction")
    private Double windDirection;

    @Column(name = "brightness")
    private Double brightness;

    @Column(name = "rainfall")
    private Double rainfall;

    protected ClimateMeasurement() {}

    public ClimateMeasurement(Reading reading, Sensor sensor, Date measuringTime, Double temperature, Double humidity, Double vaporAmount, Double windSpeed, Double windDirection, Double brightness, Double rainfall) {
        super(reading, sensor, measuringTime);
        this.temperature = temperature;
        this.humidity = humidity;
        this.vaporAmount = vaporAmount;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.brightness = brightness;
        this.rainfall = rainfall;
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

    public Double getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(Double windSpeed) {
        this.windSpeed = windSpeed;
    }

    public Double getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(Double windDirection) {
        this.windDirection = windDirection;
    }

    public Double getBrightness() {
        return brightness;
    }

    public void setBrightness(Double brightness) {
        this.brightness = brightness;
    }

    public Double getRainfall() {
        return rainfall;
    }

    public void setRainfall(Double rainfall) {
        this.rainfall = rainfall;
    }
}
