package org.salex.hmip.observer.data;

import java.util.Date;

public class ClimateMeasurement extends HomematicMeasurement {
    private final double temperature;
    private final double humidity;
    private final double vaporAmount;
    private final double windSpeed;
    private final double windDirection;
    private final double brightness;
    private final double rainfall;

    public ClimateMeasurement(Reading reading, Sensor sensor, Date measuringTime, double temperature, double humidity, double vaporAmount, double windSpeed, double windDirection, double brightness, double rainfall) {
        super(reading, sensor, measuringTime);
        this.temperature = temperature;
        this.humidity = humidity;
        this.vaporAmount = vaporAmount;
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.brightness = brightness;
        this.rainfall = rainfall;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getVaporAmount() {
        return vaporAmount;
    }

    public double getWindSpeed() {
        return windSpeed;
    }

    public double getWindDirection() {
        return windDirection;
    }

    public double getBrightness() {
        return brightness;
    }

    public double getRainfall() {
        return rainfall;
    }
}
