package com.rei.ropeteam.discovery;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jgroups.Address;
import org.jgroups.Event;
import org.jgroups.Global;
import org.jgroups.Message;
import org.jgroups.PhysicalAddress;
import org.jgroups.annotations.ManagedAttribute;
import org.jgroups.annotations.ManagedOperation;
import org.jgroups.annotations.Property;
import org.jgroups.conf.ClassConfigurator;
import org.jgroups.conf.PropertyConverters;
import org.jgroups.protocols.Discovery;
import org.jgroups.protocols.PingData;
import org.jgroups.protocols.PingHeader;
import org.jgroups.protocols.TCPPING;
import org.jgroups.util.BoundedList;
import org.jgroups.util.NameCache;
import org.jgroups.util.Responses;
import org.jgroups.util.Tuple;

public class ExternalJsonFileDiscovery extends Discovery {

    public static final short EXTERNAL_JSON_PING_ID = 2017;
    static {
        ClassConfigurator.addProtocol(EXTERNAL_JSON_PING_ID, ExternalJsonFileDiscovery.class);
    }

    private ClusterMemberService clusterMemberService;

    public ExternalJsonFileDiscovery(ClusterMemberService clusterMemberService) {
        this.clusterMemberService = clusterMemberService;
    }

    public void discoveryRequestReceived(Address sender, String logical_name, PhysicalAddress physical_addr) {
        super.discoveryRequestReceived(sender, logical_name, physical_addr);
    }

    @Override
    public void findMembers(List<Address> members, boolean initial_discovery, Responses responses) {
        PhysicalAddress physical_addr=(PhysicalAddress)down(new Event(Event.GET_PHYSICAL_ADDRESS, local_addr));

        // https://issues.jboss.org/browse/JGRP-1670
        PingData data=new PingData(local_addr, false, NameCache.get(local_addr), physical_addr);
        PingHeader hdr=new PingHeader(PingHeader.GET_MBRS_REQ).clusterName(cluster_name);

        List<PhysicalAddress> clusterMembers = clusterMemberService.getClusterMembers();

        for(final PhysicalAddress addr : clusterMembers) {
            // the message needs to be DONT_BUNDLE, see explanation above
            final Message msg=new Message(addr).setFlag(Message.Flag.INTERNAL, Message.Flag.DONT_BUNDLE, Message.Flag.OOB)
                    .putHeader(this.id,hdr).setBuffer(marshal(data));

            if(async_discovery_use_separate_thread_per_request) {
                timer.execute(() -> {
                    log.trace("%s: sending discovery request to %s", local_addr, msg.getDest());
                    down_prot.down(msg);
                });
            }
            else {
                log.trace("%s: sending discovery request to %s", local_addr, msg.getDest());
                down_prot.down(msg);
            }
        }
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
}
