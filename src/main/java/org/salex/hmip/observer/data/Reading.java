package org.salex.hmip.observer.data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table (name = "readings")
public class Reading {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reading_time", nullable = false)
    private Date readingTime;

    @OneToMany(targetEntity = Measurement.class, mappedBy = "reading", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Measurement> measurements;

    public Reading() {
        this(new Date());
    }

    public Reading(Date readingTime) {
        this(null, readingTime);
    }

    public Reading(Long id, Date readingTime) {
        this.id = id;
        this.readingTime = readingTime;
        this.measurements = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public Date getReadingTime() {
        return readingTime;
    }

    public void setReadingTime(Date readingTime) {
        this.readingTime = readingTime;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void addMeasurement(Measurement measurement) {
        this.measurements.add(measurement);
    }
}
