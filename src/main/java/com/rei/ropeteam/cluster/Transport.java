package com.rei.ropeteam.cluster;

import java.util.List;

public interface Transport {
    void send(Message message, Member member);
    List<Member> getPeers(Member member);
    boolean healthCheck(Member member);
}
