package com.github.sbridges.beacon.jmx.top;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

import com.github.sbridges.beacon.jmx.topsum.TopSum;

/**
 * configuration for {@link Top} and {@link TopSum}
 */
public final class TopConfig {

    private final Duration period;

    public TopConfig(Duration period) {
        this.period = period;
    }

    public Duration getPeriod() {
        return period;
    }

    @Override
    public int hashCode() {
        return Objects.hash(period);
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
        TopConfig other = (TopConfig) obj;
        return Objects.equals(period, other.period);
    }

    @Override
    public String toString() {
        return "TopConfig [period=" + period + "]";
    }


    
}
