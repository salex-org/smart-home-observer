package org.salex.hmip.observer.data;

import java.util.Date;

public interface ClimateMeasurementBoundaries {

    public Double getMinimumTemperature();
    public Double getMaximumTemperature();

    public Double getMinimumHumidity();
    public Double getMaximumHumidity();

    public Double getMinimumVaporAmount();
    public Double getMaximumVaporAmount();

    public Long getSensorId();

    public Date getDay();
}
