package com.github.sbridges.beacon.jmx.rate;

import java.time.Duration;

/**
 * Configuration for {@link Rate} 
 */
public final class RateConfig {

    private final String valueVield;
    private final boolean isSum;
    private final Duration period;
    
    public RateConfig(String valueVield, boolean isSum, Duration period) {
        this.valueVield = valueVield;
        this.isSum = isSum;
        this.period = period;
    }

    public String getValueVield() {
        return valueVield;
    }

    public boolean isSum() {
        return isSum;
    }

    public Duration getPeriod() {
        return period;
    }
       
}
