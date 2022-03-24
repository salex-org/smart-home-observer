package org.salex.hmip.observer.data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "homematic_measuerements")
public abstract class HomematicMeasurement extends Measurement {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sensor")
    private Sensor sensor;

    @Column(name = "measuring_time")
    private Date measuringTime;

    protected HomematicMeasurement() {}

    public HomematicMeasurement(Reading reading, Sensor sensor, Date measuringTime) {
        super(reading);
        this.sensor = sensor;
        this.measuringTime = measuringTime;
    }

    public Sensor getSensor() {
        return sensor;
    }

    public void setSensor(Sensor sensor) {
        this.sensor = sensor;
    }

    public Date getMeasuringTime() {
        return measuringTime;
    }

    public void setMeasuringTime(Date measuringTime) {
        this.measuringTime = measuringTime;
    }
}
