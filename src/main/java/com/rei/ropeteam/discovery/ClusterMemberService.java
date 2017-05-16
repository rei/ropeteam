package com.rei.ropeteam.discovery;

import static java.util.stream.Collectors.toMap;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jgroups.stack.IpAddress;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ClusterMemberService {
    private ObjectMapper objectMapper = new ObjectMapper();
    private Supplier<String> hostnameSupplier = this::lookupHostname;
    private Function<ClusterMember, UniqueStringAddress> addressLookup = this::lookupAddress;

    private URL url;
    private UniqueStringAddress currentMember;
    private String hostname;


    public ClusterMemberService(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void setHostnameSupplier(Supplier<String> hostnameSupplier) {
        this.hostnameSupplier = hostnameSupplier;
    }

    public void setAddressLookup(Function<ClusterMember, UniqueStringAddress> addressLookup) {
        this.addressLookup = addressLookup;
    }

    public Map<UniqueStringAddress, ClusterMember> getClusterMembers() {
        return readMembers().stream()
                .map(this::doAddressLookup)
//                .filter(e -> !e.getKey().equals(getCurrentMember()))
                .filter(Objects::nonNull)
                .collect(toMap(Map.Entry::getKey,
                               Map.Entry::getValue));
    }

    public synchronized UniqueStringAddress getCurrentMember() {
        if (currentMember == null) {
            currentMember = readMembers().stream()
                    .filter(m -> m.getId().equals(getHostname()))
                    .map(addressLookup)
                    .findAny()
                    .orElseThrow(() -> new IllegalStateException("cluster member list did not include current host!"));
        }
        return currentMember;
    }

    public synchronized String getHostname() {
        if (hostname == null) {
            hostname = hostnameSupplier.get();
        }
        return hostname;
    }

    private String lookupHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new UncheckedIOException(e);
        }
    }

    private UniqueStringAddress lookupAddress(ClusterMember member) {
        try {
            return new UniqueStringAddress(member.getId(), new IpAddress(member.getHost(), member.getPort()));
        } catch (UnknownHostException e) {
            return  null;
        }
    }

    private Map.Entry<UniqueStringAddress, ClusterMember> doAddressLookup(ClusterMember member) {
        return Optional.ofNullable(addressLookup.apply(member))
                .map(a -> new AbstractMap.SimpleEntry<>(a, member))
                .orElse(null);
    }

    private List<ClusterMember> readMembers() {
        try {
            return objectMapper.readValue(url, ClusterMembers.class).getMembers();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    public static class ClusterMembers {
        private List<ClusterMember> members;

        public List<ClusterMember> getMembers() {
            return members;
        }

        public void setMembers(List<ClusterMember> members) {
            this.members = members;
        }
    }

    public static class ClusterMember {
        private String id;
        private String host;
        private int port;

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

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }
}
