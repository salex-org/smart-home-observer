package org.salex.hmip.observer.controller;

import org.salex.hmip.observer.data.ClimateMeasurement;
import org.salex.hmip.observer.data.ObserverDatabase;
import org.salex.hmip.observer.data.Sensor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ClimateRestController {
    private final ObserverDatabase database;

    public ClimateRestController(ObserverDatabase database) {
        this.database = database;
    }

    @GetMapping("/climate/past")
    public Map<Sensor, List<ClimateMeasurement>> getPastClimateMeasurements(@RequestParam(name = "hours", defaultValue = "1") int hours) {
        return this.database.getClimateMeasurements(hours);
    }
}
