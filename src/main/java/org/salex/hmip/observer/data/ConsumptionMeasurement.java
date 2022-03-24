package org.salex.hmip.observer.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "consumption_measuerements")
public class ConsumptionMeasurement extends HomematicMeasurement {
    @Column(name = "power")
    private Double power;

    protected ConsumptionMeasurement() {}

    public ConsumptionMeasurement(Reading reading, Sensor sensor, Date measuringTime, double power) {
        super(reading, sensor, measuringTime);
        this.power = power;
    }

    public double getPower() {
        return power;
    }

    public void setPower(Double power) {
        this.power = power;
    }
}
