package org.salex.hmip.observer.data;

import javax.persistence.*;

@Entity
@Table(name = "sensors")
public class Sensor {
    public enum Type {
        HmIP_STHO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "kind", nullable = false)
    private Type type;

    @Column(name = "sgtin", nullable = false)
    private String sgtin;

    protected Sensor() {}

    public Sensor(Long id, String name, Type type, String sgtin) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.sgtin = sgtin;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getSgtin() {
        return sgtin;
    }

    public void setSgtin(String sgtin) {
        this.sgtin = sgtin;
    }
}
