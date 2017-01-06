package com.rei.ropeteam.cluster;

import java.util.Set;

public interface MemberDiscovery {
    Set<Member> findPotentialMembers();
}
