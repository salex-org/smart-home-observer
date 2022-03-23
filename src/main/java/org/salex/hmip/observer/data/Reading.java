package org.salex.hmip.observer.data;

import java.util.Date;
import java.util.List;

public class Reading {
    private final int id;
    private final Date readingTime;
    private final List<Measurement> measurements;

    public Reading() {
        this(new Date());
    }

    public Reading(Date readingTime) {
        this(-1, readingTime);
    }

    public Reading(int id, Reading reading) {
        this(id, reading.readingTime);
        this.measurements.addAll(reading.measurements);
    }

    public Reading(int id, java.sql.Date readingTime) {
        this(id, new Date(readingTime.getTime()));
    }

    public Reading(int id, Date readingTime) {
        this.id = id;
        this.readingTime = readingTime;
        this.measurements = List.of();
    }

    public int getId() {
        return id;
    }

    public Date getReadingTime() {
        return readingTime;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }
}
