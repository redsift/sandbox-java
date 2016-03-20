package com.redsift;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ComputeRequest {
    public InputData in;
    public InputData with;
    public String[] query;
    public LookupData[] lookup;

    public ComputeRequest(@JsonProperty("in") InputData in, @JsonProperty("with") InputData with,
                          @JsonProperty("query") String[] query, @JsonProperty("lookup") LookupData[] lookup) {
        this.in = in;
        this.with = with;
        this.query = query;
        this.lookup = lookup;
    }

    public String toString() {
        return getClass().getName() + "@" + this.in + this.with + this.query + this.lookup;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class InputData {
        public String bucket;
        public Data[] data;

        public InputData(@JsonProperty("bucket") String bucket, @JsonProperty("data") Data[] data) {
            this.bucket = bucket;
            this.data = data;
        }

        public String toString() {
            return getClass().getName() + "@" + this.bucket + this.data;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LookupData {
        public String bucket;
        public Data data;

        public LookupData(@JsonProperty("bucket") String bucket, @JsonProperty("data") Data data) {
            this.bucket = bucket;
            this.data = data;
        }

        public String toString() {
            return getClass().getName() + "@" + this.bucket + this.data;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Data {
        public String key;
        public byte[] value;
        public long epoch;
        public int generation;

        public Data(@JsonProperty("key") String key, @JsonProperty("value") byte[] value,
                    @JsonProperty("epoch") long epoch, @JsonProperty("generation") int generation) {
            this.key = key;
            this.value = value;
            this.epoch = epoch;
            this.generation = generation;
        }

        public String toString() {
            return getClass().getName() + "@" + this.key + this.value + this.epoch + this.generation;
        }
    }
}
