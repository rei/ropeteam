package com.rei.ropeteam.discovery;

import java.util.List;

public class ClusterMembers {
    private List<ClusterMember> members;

    public ClusterMembers() {
    }

    public ClusterMembers(List<ClusterMember> members) {
        this.members = members;
    }

    public List<ClusterMember> getMembers() {
        return members;
    }

    public void setMembers(List<ClusterMember> members) {
        this.members = members;
    }
}
