package com.rei.ropeteam;

public interface OncePerClusterExecutor {
    boolean canExecute(String cmdName);

    void execute(String cmdName, Action cmd) throws Throwable;
}
