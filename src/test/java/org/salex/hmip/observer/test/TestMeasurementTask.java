package org.salex.hmip.observer.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salex.hmip.observer.data.ObserverDatabase;
import org.salex.hmip.observer.service.ClimateMeasurementService;
import org.salex.hmip.observer.service.OperatingMeasurementService;
import org.salex.hmip.observer.task.MeasurementTask;
import static org.mockito.Mockito.mock;

public class TestMeasurementTask {
    private ObserverDatabase database;
    private ClimateMeasurementService climateMeasurementService;
    private OperatingMeasurementService operatingMeasurementService;
    private MeasurementTask task;

    @BeforeEach
    void setup() {
        database = mock(ObserverDatabase.class);
        climateMeasurementService = mock(ClimateMeasurementService.class);
        operatingMeasurementService = mock(OperatingMeasurementService.class);
        task = new MeasurementTask("0 0/1 * * * *", database, operatingMeasurementService, climateMeasurementService);
    }

    @Test
    void testMeasurement() {

    }
}
