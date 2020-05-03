package com.github.sbridges.beacon.listeners;

public interface KeyValueListener extends Flushable {

    void hear(String key, double value);
}
