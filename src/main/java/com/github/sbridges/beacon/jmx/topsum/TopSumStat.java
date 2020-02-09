package com.github.sbridges.beacon.jmx.topsum;

import java.util.Objects;

public final class TopSumStat implements TopSumStatMBean, Comparable<TopSumStat> {

    private final String key;
    private final double value;
    private final int events;
    private final long totalDuration;

    
    public TopSumStat(String key, double value, int events,
            long totalDuration) {
        this.key = key;
        this.value = value;
        this.events = events;
        this.totalDuration = totalDuration;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public int events() {
        return events;
    }

    @Override
    public long getTotalDuration() {
        return totalDuration;
    }

    @Override
    public int compareTo(TopSumStat o) {
        int result = Double.compare(o.value, this.value);
        if(result != 0) {
            return result;
        }
        result = Integer.compare(o.events, this.events);
        if(result != 0) {
            return result;
        }
        result = Long.compare(o.totalDuration, this.totalDuration);
        if(result != 0) {
            return result;
        }
        return o.key.compareTo(this.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(events, key, totalDuration, value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TopSumStat other = (TopSumStat) obj;
        return events == other.events && Objects.equals(key, other.key)
                && totalDuration == other.totalDuration
                && Double.doubleToLongBits(value) == Double
                        .doubleToLongBits(other.value);
    }

    @Override
    public String toString() {
        return "TopSumStat [key=" + key + ", value=" + value + ", events="
                + events + ", totalDuration=" + totalDuration + "]";
    }
        
   

}
