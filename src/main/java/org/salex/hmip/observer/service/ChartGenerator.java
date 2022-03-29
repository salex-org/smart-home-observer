package org.salex.hmip.observer.service;

import org.salex.hmip.observer.data.ClimateMeasurement;

import java.io.IOException;
import java.util.List;

public interface ChartGenerator {
    byte[] create24HourChart(List<ClimateMeasurement> data) throws IOException;
}
