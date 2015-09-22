package com.rei.ropeteam;

import org.jgroups.JChannel;
import org.jgroups.fork.ForkChannel;
import org.jgroups.protocols.FORK;
import org.jgroups.stack.Protocol;

public class Forks {
    public static ForkChannel fork(JChannel main, String id, Protocol... extraProtocols) {
        try {
            FORK forkProto = new FORK();
            forkProto.setProtocolStack(main.getProtocolStack());
            main.getProtocolStack().insertProtocolAtTop(forkProto);
            
            ForkChannel fork = new ForkChannel(main, id, id, extraProtocols);
            fork.connect(main.getClusterName());
            
            return fork;
        } catch (Exception e) {
            throw new RuntimeException("unable to create fork channel: " + id, e);
        }
    }
}
