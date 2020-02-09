package com.github.sbridges.beacon.internal;

import com.github.sbridges.beacon.jmx.topsum.TopSumStat;

public final class TopIntermediateResults {

    private double value;
    private int events;
    private  long totalDuration;
    
    public TopIntermediateResults() {
        
    }
    
    public void add(double addToValue, long duration) {
        value += addToValue;
        totalDuration += duration;
        events++;
    }
    
    public void set(double newValue) {
        value = newValue;
    }
    
    public TopSumStat complete(String key) {
        return new TopSumStat(key, value, events, totalDuration);
    }

    @Override
    public String toString() {
        return "TopIntermediateResults [value=" + value + ", events="
                + events + ", totalDuration=" + totalDuration + "]";
    }


 
    
    
}
