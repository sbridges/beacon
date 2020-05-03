package com.github.sbridges.beacon.jmx.gauge;

import com.github.sbridges.beacon.listeners.DoubleValueListener;
import com.github.sbridges.beacon.internal.FirstException;

/**
 * Implementation of {@link GaugeMXBean} 
 */
public final class Gauge implements GaugeMXBean, DoubleValueListener {

    private volatile double value = 0;
    private final FirstException firstException = new FirstException();
    
    public Gauge() {}

    @Override
    public void hear(double value) {
        this.value = value;
    }

    @Override
    public double getValue() {
        return value;
    }

    @Override
    public String[] getFirstException() {
        return firstException.get();
    }
    
    @Override
    public void flush() {}

    @Override
    public String toString() {
        return "Gauge []";
    }

    @Override
    public void hearException(Exception e) {
        firstException.hear(e);
    }
}
