package com.github.sbridges.beacon.jmx.top;

import java.util.Objects;

public final class TopStat implements TopStatMBean, Comparable<TopStat> {

    private final String key;
    private final double value;

    public TopStat(String key, double value) {
        this.key = key;
        this.value = value;
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
    public int compareTo(TopStat o) {
        int result = Double.compare(o.value, this.value);
        if(result != 0) {
            return result;
        }
        return o.key.compareTo(o.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
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
        TopStat other = (TopStat) obj;
        return Objects.equals(key, other.key)
                && Double.doubleToLongBits(value) == Double
                        .doubleToLongBits(other.value);
    }

    @Override
    public String toString() {
        return "TopStat [key=" + key + ", value=" + value + "]";
    }

    
}
