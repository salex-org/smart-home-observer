package org.salex.hmip.observer.data;

import java.util.Date;

public abstract class HomematicMeasurement extends Measurement {
    private final Sensor sensor;
    private final Date measuringTime;

    public HomematicMeasurement(Reading reading, Sensor sensor, Date measuringTime) {
        super(reading);
        this.sensor = sensor;
        this.measuringTime = measuringTime;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public Date getMeasuringTime() {
        return measuringTime;
    }
}
