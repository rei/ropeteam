package com.rei.ropeteam.cluster;

import java.util.HashMap;
import java.util.Map;

public class Message {
    public static final String TYPE_HEADER = "_type";
    private Map<String, String> headers = new HashMap<>();
    private String body;

    public Message() {}

    public Message(Map<String, String> headers, String body) {
        this.headers = headers;
        this.body = body;
    }

    public String getType() {
        return headers.getOrDefault(TYPE_HEADER, Cluster.DEFAULT_TYPE);
    }

    public void setType(String type) {
        headers.put(TYPE_HEADER, type);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
