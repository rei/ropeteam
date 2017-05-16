package com.rei.ropeteam.discovery;

import static java.util.stream.Collectors.toList;

import java.util.Comparator;
import java.util.List;

import org.jgroups.Address;
import org.jgroups.conf.ClassConfigurator;
import org.jgroups.protocols.Discovery;
import org.jgroups.protocols.PingData;
import org.jgroups.util.Responses;

public class ExternalJsonFileDiscovery extends Discovery {

    public static final short EXTERNAL_JSON_PING_ID = 2017;
    static {
        ClassConfigurator.addProtocol(EXTERNAL_JSON_PING_ID, ExternalJsonFileDiscovery.class);
    }

    private ClusterMemberService clusterMemberService;

    public ExternalJsonFileDiscovery(ClusterMemberService clusterMemberService) {
        this.clusterMemberService = clusterMemberService;
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    @Override
    protected void findMembers(List<Address> members, boolean initial_discovery, Responses responses) {

        List<PingData> pings = clusterMemberService.getClusterMembers().entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getValue().getId()))
                .map(e -> new PingData(new UniqueStringAddress(e.getValue().getId(), e.getKey()), false))
                .collect(toList());

        if (!pings.isEmpty()) {
            pings.get(0).coord(true);
        }

        pings.stream()
                .filter(pd -> !pd.getAddress().equals(clusterMemberService.getCurrentMember())) // filter ourselves out
                .forEach(p -> responses.addResponse(p, true));
    }
}
