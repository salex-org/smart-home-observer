package org.salex.hmip.observer.service;

import org.salex.hmip.observer.data.OperatingMeasurement;

import java.util.Date;
import java.util.List;

public interface OperatingAlertService {
    abstract class Event {
        private final Date timestamp;

        public Event() {
            this.timestamp = new Date();
        }

        public Date getTimestamp() {
            return timestamp;
        }
    }
    class Error extends Event {
        private final Throwable error;

        public Error(Throwable error) {
            this.error = error;
        }

        public Throwable getError() {
            return error;
        }

        public Throwable getRootCause() {
            return getRootCause(error);
        }

        private Throwable getRootCause(Throwable error) {
            if(error.getCause() != null) {
                return getRootCause(error.getCause());
            } else {
                return error;
            }
        }
    }
    class Exceedance extends Event {
        private final OperatingMeasurement measurement;

        public Exceedance(OperatingMeasurement measurement) {
            this.measurement = measurement;
        }

        public OperatingMeasurement getMeasurement() {
            return measurement;
        }
    }

    void signal(Throwable error);

    void check(List<OperatingMeasurement> measurements);

    List<Event> retrieveEvents();
}
