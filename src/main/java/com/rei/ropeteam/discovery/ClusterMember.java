package com.rei.ropeteam.discovery;

public class ClusterMember {
    private String host;
    private int port;

    public ClusterMember() {
    }

    public ClusterMember(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
