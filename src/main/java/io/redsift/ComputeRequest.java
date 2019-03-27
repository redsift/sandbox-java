package io.redsift;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;

@JsonInclude(content= JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ComputeRequest {
    public InputData in;
    public InputData with;
    public String[] query;
    public GetData[] get;

    public ComputeRequest(@JsonProperty("in") InputData in, @JsonProperty("with") InputData with,
                          @JsonProperty("query") String[] query, @JsonProperty("get") GetData[] get) {
        this.in = in;
        this.with = with;
        this.query = query;
        this.get = get;
    }

    public String toString() {
        return "[in: " + this.in + ", " + "with: " + this.with + ", " + "query: " + Arrays.toString(this.query) + ", " +
                "get: " + Arrays.toString(this.get) + "]";
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
    public static class GetData {
        public String bucket;
        public String key;
        public Data[] data;

        public GetData(@JsonProperty("bucket") String bucket, @JsonProperty("key") String key,
                       @JsonProperty("data") Data[] data) {
            this.bucket = bucket;
            this.key = key;
            this.data = data;
        }

        public String toString() {
            return "[bucket: " + this.bucket + ", key: " + this.key + ", data: " + this.data + "]";
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
