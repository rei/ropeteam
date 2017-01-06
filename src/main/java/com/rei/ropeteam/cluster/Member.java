package com.rei.ropeteam.cluster;

import java.util.Objects;

public class Member {
    private String host;
    private int port;

    public Member() {}

    public Member(String host, int port) {
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

    @Override
    public String toString() {
        return host + ":" + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Member)) {
            return false;
        }
        Member member = (Member) o;
        return port == member.port &&
                Objects.equals(host, member.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }
}
