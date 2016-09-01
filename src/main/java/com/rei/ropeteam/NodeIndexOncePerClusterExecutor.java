package com.rei.ropeteam;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeIndexOncePerClusterExecutor implements OncePerClusterExecutor {

    private static final Logger logger = LoggerFactory.getLogger(NodeIndexOncePerClusterExecutor.class);
    public static final String NODE_INDEX = "NODE_INDEX";
    private Optional<String> nodeIndex;

    public NodeIndexOncePerClusterExecutor() {
        this(System.getenv(NODE_INDEX));
    }

    public NodeIndexOncePerClusterExecutor(String nodeIndex) { // to make testing easier
        this.nodeIndex = Optional.ofNullable(nodeIndex);
    }

    @Override
    public boolean canExecute(String cmdName) {
        return nodeIndex.map(ni -> ni.trim().equals("0")).orElse(false);
    }
    
    @Override
    public void execute(String cmdName, Action cmd) throws Throwable {
        if (canExecute(cmdName)) {
            logger.info("running {} on node index {}", cmdName, nodeIndex.get());
            cmd.execute();
        } else {
            logger.info("skipping {} on {}", cmdName, nodeIndex.get());
        }
    }
}
