package com.rei.ropeteam;

import java.lang.reflect.Field;

import org.jgroups.JChannel;
import org.jgroups.blocks.locking.LockService;
import org.jgroups.protocols.CENTRAL_LOCK;
import org.jgroups.stack.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OncePerClusterExecutor {
    
    private static final Logger logger = LoggerFactory.getLogger(OncePerClusterExecutor.class);
    
    private LockService lockService;
    private JChannel channel;
    
    public OncePerClusterExecutor(JChannel channel) {
        this.channel = channel;
        lockService = new LockService(Forks.fork(channel, "once-per-cluster", createLockingProtocol()));
    }

    private Protocol createLockingProtocol() {
        CENTRAL_LOCK locking = new CENTRAL_LOCK();
        Field field;
        try {
            field = CENTRAL_LOCK.class.getDeclaredField("use_thread_id_for_lock_owner");
            field.setAccessible(true);
            field.set(locking, false);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException("unable to create locking protocol", e);
        }
        return locking;
    }
    
    public boolean canExecute(String cmdName) {
        return lockService.getLock(cmdName).tryLock();
    }
    
    public void execute(String cmdName, Runnable cmd) {
        if (canExecute(cmdName)) {
            logger.info("running {} on {}", cmdName, channel.getAddressAsString());
            cmd.run();
        }
    }
}
