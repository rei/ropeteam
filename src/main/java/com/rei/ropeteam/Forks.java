package com.rei.ropeteam;

import org.jgroups.JChannel;
import org.jgroups.fork.ForkChannel;
import org.jgroups.protocols.FORK;
import org.jgroups.stack.Protocol;

public class Forks {
    public static ForkChannel fork(JChannel main, String id, Protocol... extraProtocols) {
        return fork(main, id, true, extraProtocols);
    }
    
    public static ForkChannel fork(JChannel main, String id, boolean connect, Protocol... extraProtocols) {
        try {
            main.getProtocolStack().insertProtocolAtTop(new FORK().setProtocolStack(main.getProtocolStack()));
            
            ForkChannel fork = new ForkChannel(main, id, id, extraProtocols);
            
            if (connect) {
                fork.connect(main.getClusterName());
            }
            
            return fork;
        } catch (Exception e) {
            throw new RuntimeException("unable to create fork channel: " + id, e);
        }
    }
}
