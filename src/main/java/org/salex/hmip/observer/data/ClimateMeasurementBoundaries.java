package org.salex.hmip.observer.data;

import java.util.Date;

public class ClimateMeasurementBoundaries {
    private final double minimumTemperature;
    private final double maximumTemperature;
    private final double minimumHumidity;
    private final double maximumHumidity;
    private final double minimumVaporAmount;
    private final double maximumVaporAmount;
    private final double minimumWindSpeed;
    private final double maximumWindSpeed;
    private final double minimumBrightness;
    private final double maximumBrightness;
    private final double minimumRainfall;
    private final double maximumRainfall;
    private final Sensor sensor;
    private final Date day;

    public ClimateMeasurementBoundaries(double minimumTemperature, double maximumTemperature, double minimumHumidity, double maximumHumidity, double minimumVaporAmount, double maximumVaporAmount, double minimumWindSpeed, double maximumWindSpeed, double minimumBrightness, double maximumBrightness, double minimumRainfall, double maximumRainfall, Sensor sensor, Date day) {
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

    public double getMinimumTemperature() {
        return minimumTemperature;
    }

    public double getMaximumTemperature() {
        return maximumTemperature;
    }

    public double getMinimumHumidity() {
        return minimumHumidity;
    }

    public double getMaximumHumidity() {
        return maximumHumidity;
    }

    public double getMinimumVaporAmount() {
        return minimumVaporAmount;
    }

    public double getMaximumVaporAmount() {
        return maximumVaporAmount;
    }

    public double getMinimumWindSpeed() {
        return minimumWindSpeed;
    }

    public double getMaximumWindSpeed() {
        return maximumWindSpeed;
    }

    public double getMinimumBrightness() {
        return minimumBrightness;
    }

    public double getMaximumBrightness() {
        return maximumBrightness;
    }

    public double getMinimumRainfall() {
        return minimumRainfall;
    }

    public double getMaximumRainfall() {
        return maximumRainfall;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public Date getDay() {
        return day;
    }
}
