package com.redsift;

/**
 * Created by deepakp on 19/03/2016.
 */
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
        return getClass().getName() + "@" + this.name + this.key + this.value + this.epoch;
    }

}
