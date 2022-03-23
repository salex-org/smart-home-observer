package org.salex.hmip.observer.data;

import java.util.Date;

public class OperatingMeasurement extends Measurement {
    private final double cpuTemperature;
    private final double coreVoltage;
    private final double diskUsage;
    private final double memoryUsage;

    public OperatingMeasurement(Reading reading, double cpuTemperature, double coreVoltage, double diskUsage, double memoryUsage) {
        super(reading);
        this.cpuTemperature = cpuTemperature;
        this.coreVoltage = coreVoltage;
        this.diskUsage = diskUsage;
        this.memoryUsage = memoryUsage;
    }

    public double getCpuTemperature() {
        return cpuTemperature;
    }

    public double getCoreVoltage() {
        return coreVoltage;
    }

    public double getDiskUsage() {
        return diskUsage;
    }

    public double getMemoryUsage() {
        return memoryUsage;
    }
}
