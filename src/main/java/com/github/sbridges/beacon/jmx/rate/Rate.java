package com.github.sbridges.beacon.jmx.rate;

import java.time.Clock;
import java.util.Objects;

import com.github.sbridges.beacon.RecordedEventListener;
import com.github.sbridges.beacon.internal.FirstException;

import jdk.jfr.consumer.RecordedEvent;

/**
 * Implementation of {@link RateMXBean}
 */
public final class Rate implements RecordedEventListener, RateMXBean {

    private final FirstException firstException = new FirstException();

    private long lastFlushTime;
    private volatile double lastFlushValue;

    private final Clock clock;

    private double value;
    private volatile double rate;
    private final RateConfig rateConfig;

    public Rate(RateConfig rateConfig, Clock clock) {
        this.rateConfig = rateConfig;
        this.clock = clock;
    }

    @Override
    public void hear(RecordedEvent e) {
        try {
            double newValue = e.getDouble(rateConfig.getValueVield());
            if (rateConfig.isSum()) {
                value += newValue;
            } else {
                value = newValue;
            }

        } catch (RuntimeException ex) {
            firstException.hear(ex);
        }
    }

    @Override
    public void flush() {
        long now = clock.millis();

        if (lastFlushTime + rateConfig.getPeriod().toMillis() <= now) {
            long elapsed = now - lastFlushTime;
            // we can't calculate the rate for the first flush
            // so set elapsed to 0
            if (lastFlushTime == 0) {
                elapsed = 0;
            }

            double delta = value - lastFlushValue;

            if (elapsed > 0) {
                rate = delta / elapsed * 1000;
            } else {
                rate = 0;
            }

            lastFlushTime = now;
            lastFlushValue = value;
        }
    }

    @Override
    public double getRatePerSecond() {
        return rate;
    }

    @Override
    public double getTotal() {
        return lastFlushValue;
    }

    @Override
    public String[] getFirstException() {
        return firstException.get();
    }

    // VisibleForTesting
    public RateConfig getRateConfig() {
        return rateConfig;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rateConfig);
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
        Rate other = (Rate) obj;
        return Objects.equals(rateConfig, other.rateConfig);
    }

    @Override
    public String toString() {
        return "Rate [rateConfig=" + rateConfig + "]";
    }

}
