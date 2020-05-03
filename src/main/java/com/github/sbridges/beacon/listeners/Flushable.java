package com.github.sbridges.beacon.listeners;

public interface Flushable {

    void flush();

    void hearException(Exception e);
}
