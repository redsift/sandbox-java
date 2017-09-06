package io.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;
import java.util.Map;

@JsonInclude(content = JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Request {
    public String method;
    public String requestUri;
    public Map<String, String[]> header;
    public byte[] body;

    public Request(@JsonProperty("method") String method, 
      @JsonProperty("request_uri") String requestUri,
      @JsonProperty("header") Map<String, String[]> header, 
      @JsonProperty("body") byte[] body) {

        this.method = method;
        this.requestUri = requestUri;
        this.header = header;
        this.body = body;
    }

    public Request(byte[] b) throws IOException {
        Request r = mapper.readValue(b, Request.class);
        this.method = r.method;
        this.requestUri = r.requestUri;
        this.header = r.header;
        this.body = r.body;
    }

}