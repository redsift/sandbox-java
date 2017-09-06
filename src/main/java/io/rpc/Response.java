package io.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonInclude(content = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {
    public int statusCode;
    public Map<String, String[]> header;
    public byte[] body;

    public Response( @JsonProperty("status_code") int statusCode,
        @JsonProperty("header") Map<String, String[]> header, 
        @JsonProperty("body") byte[] body) {

        this.statusCode = statusCode;
        this.header = header;
        this.body = body;
    }

}