package org.salex.hmip.observer.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "operating_measuerements")
public class OperatingMeasurement extends Measurement {
    @Column(name="cpu_temperature")
    private Double cpuTemperature;

    @Column(name="core_voltage")
    private Double coreVoltage;

    @Column(name="disk_usage")
    private Double diskUsage;

    @Column(name="memory_usage")
    private Double memoryUsage;

    protected OperatingMeasurement() {}

    public OperatingMeasurement(Reading reading, Double cpuTemperature, Double coreVoltage, Double diskUsage, Double memoryUsage) {
        super(reading);
        this.cpuTemperature = cpuTemperature;
        this.coreVoltage = coreVoltage;
        this.diskUsage = diskUsage;
        this.memoryUsage = memoryUsage;
    }

    public Double getCpuTemperature() {
        return cpuTemperature;
    }

    public void setCpuTemperature(Double cpuTemperature) {
        this.cpuTemperature = cpuTemperature;
    }

    public Double getCoreVoltage() {
        return coreVoltage;
    }

    public void setCoreVoltage(Double coreVoltage) {
        this.coreVoltage = coreVoltage;
    }

    public Double getDiskUsage() {
        return diskUsage;
    }

    public void setDiskUsage(Double diskUsage) {
        this.diskUsage = diskUsage;
    }

    public Double getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(Double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }
}
