package io.github.flock;

import org.jgroups.JChannel;
import org.jgroups.fork.ForkChannel;
import org.jgroups.stack.Protocol;
import org.jgroups.stack.ProtocolStack;

public class Forks {
    public static ForkChannel fork(JChannel main, String id, Protocol... extraProtocols) {
        try {
            ForkChannel fork = new ForkChannel(main, id, id, true, 
                    ProtocolStack.ABOVE, 
                    main.getProtocolStack().getTopProtocol().getClass(), 
                    extraProtocols);
            
            fork.connect(main.getClusterName());
            
            return fork;
        } catch (Exception e) {
            throw new RuntimeException("unable to create fork channel: " + id, e);
        }
    }
}
