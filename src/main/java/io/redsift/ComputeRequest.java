package io.redsift;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

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
        return "[in: " + this.in + ", " + "with: " + this.with + ", " + "query: " + Arrays.toString(this.query) + ", " +
                "lookup: " + Arrays.toString(this.lookup) + "]";
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
            return "[bucket: " + this.bucket + ", " + "data[]: " + Arrays.toString(this.data) + "]";
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
            return "[bucket: " + this.bucket + ", " + "data: " + this.data + "]";
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
            return "[key: " + this.key + ", " + "value: " +
                    (this.value != null ? new String(this.value) : this.value) + ", " + "epoch: " + this.epoch + ", "
                    + "generation: " + this.generation + "]";
        }
    }
}
