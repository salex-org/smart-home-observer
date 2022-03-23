package org.salex.hmip.observer.data;

import java.util.Date;

public class ConsumptionMeasurement extends HomematicMeasurement {
    private final double power;

    public ConsumptionMeasurement(Reading reading, Sensor sensor, Date measuringTime, double power) {
        super(reading, sensor, measuringTime);
        this.power = power;
    }

    public double getPower() {
        return power;
    }
}
