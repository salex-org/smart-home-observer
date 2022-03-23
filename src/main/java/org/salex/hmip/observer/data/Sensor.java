package org.salex.hmip.observer.data;

public class Sensor {
    public static enum Type {
        HmIP_STHO
    }

    private final int id;
    private final String name;
    private final Type type;
    private final String sgtin;

    public Sensor(int id, String name, String type, String sgtin) {
        this.id = id;
        this.name = name;
        this.type = Type.valueOf(type);
        this.sgtin = sgtin;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public String getSgtin() {
        return sgtin;
    }
}
