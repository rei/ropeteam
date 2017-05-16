package com.rei.ropeteam.discovery;

import org.jgroups.PhysicalAddress;
import org.jgroups.protocols.TCP_NIO2;

public class DynamicPortTcpNio2 extends TCP_NIO2 {
    private ClusterMemberService clusterMemberService;

    public DynamicPortTcpNio2(ClusterMemberService clusterMemberService) {
        this.clusterMemberService = clusterMemberService;
    }

    @Override
    protected PhysicalAddress getPhysicalAddress() {
        return clusterMemberService.getCurrentMember();
    }
}
