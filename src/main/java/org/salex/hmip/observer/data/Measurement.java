package org.salex.hmip.observer.data;

public abstract class Measurement {
    private final Reading reading;

    public Measurement(Reading reading) {
        this.reading = reading;
    }

    public Reading getReading() {
        return reading;
    }
}
