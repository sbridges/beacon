package com.github.sbridges.beacon.jmx.rate;

import java.time.Duration;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * Configuration for {@link Rate} 
 */
public final class RateConfig {

    private final boolean isSum;
    private final Duration period;
    
    public RateConfig(boolean isSum, Duration period) {
        this.isSum = isSum;
        this.period = period;
    }

    public boolean isSum() {
        return isSum;
    }

    public Duration getPeriod() {
        return period;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RateConfig that = (RateConfig) o;
        return isSum == that.isSum &&
                Objects.equals(period, that.period);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isSum, period);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RateConfig.class.getSimpleName() + "[", "]")
                .add("isSum=" + isSum)
                .add("period=" + period)
                .toString();
    }
}
