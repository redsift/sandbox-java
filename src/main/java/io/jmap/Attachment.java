package io.jmap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(content = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Attachment {
    public String blobId;
    public String type;
    public String name;
    public long size;
    public boolean isInline;
    public long width;
    public long height;

    public Attachment(@JsonProperty("blobId") String blobId, @JsonProperty("type") String type,
                      @JsonProperty("name") String name, @JsonProperty("size") long size,
                      @JsonProperty("isInline") boolean isInline, @JsonProperty("width") long width,
                      @JsonProperty("height") long height) {
        this.blobId = blobId;
        this.type = type;
        this.name = name;
        this.size = size;
        this.isInline = isInline;
        this.width = width;
        this.height = height;
    }
}
