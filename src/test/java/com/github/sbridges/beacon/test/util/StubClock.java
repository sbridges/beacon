package com.github.sbridges.beacon.test.util;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

public class StubClock extends Clock {

    private long nextTime = System.currentTimeMillis();
    
    public void advanceSeconds(long seconds) {
        advanceMillis(TimeUnit.SECONDS.toMillis(seconds));
    }
    
    public void advanceMillis(long millis) {
        nextTime += millis;
    }
    
    @Override
    public ZoneId getZone() {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public Clock withZone(ZoneId zone) {
        throw new IllegalStateException("not implemented");
    }

    @Override
    public Instant instant() {
        return Instant.ofEpochMilli(nextTime);
    }

    
    
}
