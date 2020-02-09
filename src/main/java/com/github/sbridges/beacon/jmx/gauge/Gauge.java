package com.github.sbridges.beacon.jmx.gauge;

import java.util.Objects;

import com.github.sbridges.beacon.RecordedEventListener;
import com.github.sbridges.beacon.internal.FirstException;

import jdk.jfr.consumer.RecordedEvent;

/**
 * Implementation of {@link GaugeMXBean} 
 */
public final class Gauge implements GaugeMXBean, RecordedEventListener {

    private final String eventField;   
    private volatile double value = 0;
    private final FirstException firstException = new FirstException();
    
    public Gauge(GaugeConfig config) {
        eventField = config.getEventField();
    }
    
    @Override
    public void hear(RecordedEvent e) {
        try {
            value = e.getDouble(eventField);
        } catch(RuntimeException re) {
            firstException.hear(re);
        }
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
        return "Gauge [eventField=" + eventField + "]";
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventField);
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
        Gauge other = (Gauge) obj;
        return Objects.equals(eventField, other.eventField);
    }

    public String getEventField() {
        return eventField;
    }
    
    
    
}
