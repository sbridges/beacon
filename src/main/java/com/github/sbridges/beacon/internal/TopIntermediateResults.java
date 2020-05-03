package com.github.sbridges.beacon.internal;

import com.github.sbridges.beacon.jmx.topsum.TopSumStat;

public final class TopIntermediateResults {

    private double value;
    private int events;
    private  long totalDurationNanos;
    
    public TopIntermediateResults() {
        
    }
    
    public void add(double addToValue, long durationNanos) {
        value += addToValue;
        totalDurationNanos += durationNanos;
        events++;
    }
    
    public void set(double newValue) {
        value = newValue;
    }
    
    public TopSumStat complete(String key) {
        return new TopSumStat(key, value, events, totalDurationNanos);
    }

    @Override
    public String toString() {
        return "TopIntermediateResults [value=" + value + ", events="
                + events + ", totalDurationNanos=" + totalDurationNanos + "]";
    }

    
}
