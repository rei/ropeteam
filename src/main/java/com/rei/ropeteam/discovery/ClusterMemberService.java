package com.rei.ropeteam.discovery;

import static java.util.stream.Collectors.toList;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.jgroups.PhysicalAddress;
import org.jgroups.stack.IpAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ClusterMemberService {
    private static final Logger logger = LoggerFactory.getLogger(ClusterMemberService.class);

    private ObjectMapper objectMapper = new ObjectMapper();
    private Function<ClusterMember, IpAddress> addressLookup = this::lookupAddress;

    private URL url;


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

    public void setAddressLookup(Function<ClusterMember, IpAddress> addressLookup) {
        this.addressLookup = addressLookup;
    }

    public List<PhysicalAddress> getClusterMembers() {
        return readMembers().stream()
                .map(this::doAddressLookup)
                .filter(Objects::nonNull)
                .collect(toList());
    }

    private IpAddress lookupAddress(ClusterMember member) {
        try {
            return new IpAddress(member.getHost(), member.getPort());
        } catch (UnknownHostException e) {
            return  null;
        }
    }

    private IpAddress doAddressLookup(ClusterMember member) {
        return Optional.ofNullable(addressLookup.apply(member)).orElse(null);
    }

    private List<ClusterMember> readMembers() {
        try {
            return objectMapper.readValue(url, ClusterMembers.class).getMembers();
        } catch (IOException e) {
            logger.error("error getting cluster member list", e);
            return new ArrayList<>();
        }
    }

}
