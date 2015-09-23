package com.rei.ropeteam;

@FunctionalInterface
public interface Action {
    void execute() throws Throwable;
}
