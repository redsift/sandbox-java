package com.redsift;

public class ComputeResponse {
    public String name;
    public String key;
    public Object value;
    public long epoch;

    public ComputeResponse(String name, String key, Object value, long epoch) {
        this.name = name;
        this.key = key;
        this.value = value;
        this.epoch = epoch;
    }

    public String toString() {
        return "[name: " + this.name + ", " + "key: " + this.key + ", " + "value: " + this.value + ", " +
                "epoch: " + this.epoch + "]";
    }

}
