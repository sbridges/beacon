package com.github.sbridges.beacon.jmx.topsum;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public final class TopSumStat implements TopSumStatMBean, Comparable<TopSumStat> {

    private final String key;
    private final double value;
    private final int events;
    private final long totalDurationNanos;

    
    public TopSumStat(String key, double value, int events,
            long totalDurationNanos) {
        this.key = key;
        this.value = value;
        this.events = events;
        this.totalDurationNanos = totalDurationNanos;
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
    public long getTotalDurationNanos() {
        return totalDurationNanos;
    }

    @Override
    public long getTotalDurationMillis() {
        return TimeUnit.MILLISECONDS.toSeconds(totalDurationNanos);
    }


    @Override
    public long getTotalDurationSeconds() {
        return TimeUnit.NANOSECONDS.toSeconds(totalDurationNanos);
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
        result = Long.compare(o.totalDurationNanos, this.totalDurationNanos);
        if(result != 0) {
            return result;
        }
        return o.key.compareTo(this.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(events, key, totalDurationNanos, value);
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
                && totalDurationNanos == other.totalDurationNanos
                && Double.doubleToLongBits(value) == Double
                        .doubleToLongBits(other.value);
    }

    @Override
    public String toString() {
        return "TopSumStat [key=" + key + ", value=" + value + ", events="
                + events + ", totalDurationNanos=" + totalDurationNanos + "]";
    }
        
   

}
