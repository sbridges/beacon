package com.github.sbridges.beacon.listeners;

import java.time.Duration;

public interface KeyValueDurationListener extends Flushable {

    void hear(String key, double value, Duration duration);
}
